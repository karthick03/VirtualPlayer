package com.android.lab.virtualplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import android.os.Environment;

class StorageHelper {

    private static final String TAG = "StorageHelper";

    public StorageHelper() {
    }

    public String getExternalSDPath() {
        StorageVolume sv = this.getStorage(StorageVolume.Type.EXTERNAL);
        if (sv != null)
            return sv.file.getPath();
        else {
            String sdpath = System.getenv("SECONDARY_STORAGE");
            return (sdpath == null || sdpath.isEmpty()) ? "/storage/extSdCard" : sdpath;
        }
    }

    public String getInternalSDPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public boolean externalSDAvailable() {
        String path = getExternalSDPath();
        if (path != null) {
            File file = new File(path);
            return file.exists() && file.list() != null;
        }
        return false;
    }

    private static final String STORAGES_ROOT;

    static {
        String primaryStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        int index = primaryStoragePath.indexOf(File.separatorChar, 1);
        STORAGES_ROOT = index != -1 ? primaryStoragePath.substring(0, index + 1) : File.separator;
    }

    private static final String[] AVOIDED_DEVICES = new String[] { "rootfs", "tmpfs", "dvpts", "proc", "sysfs", "none" };

    private static final String[] AVOIDED_DIRECTORIES = new String[] { "obb", "asec" };

    private static final String[] DISALLOWED_FILESYSTEMS = new String[] { "tmpfs", "rootfs", "romfs", "devpts", "sysfs", "proc", "cgroup", "debugfs" };

    public  List<StorageVolume> getStorages(final boolean includeUsb) {
        final Map<String, List<StorageVolume>> deviceVolumeMap = new HashMap<>();

        // this approach considers that all storages are mounted in the same
        // non-root directory
        if (!STORAGES_ROOT.equals(File.separator)) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader("/proc/mounts"));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Log.d(TAG, line);
                    final StringTokenizer tokens = new StringTokenizer(line, " ");

                    final String device = tokens.nextToken();
                    // skipped devices that are not sdcard for sure
                    if (arrayContains(AVOIDED_DEVICES, device))
                        continue;

                    // should be mounted in the same directory to which
                    // the primary external storage was mounted
                    final String path = tokens.nextToken();
                    if (!path.startsWith(STORAGES_ROOT))
                        continue;

                    // skip directories that indicate tha volume is not a
                    // storage volume
                    if (pathContainsDir(path, AVOIDED_DIRECTORIES))
                        continue;

                    final String fileSystem = tokens.nextToken();
                    // don't add ones with non-supported filesystems
                    if (arrayContains(DISALLOWED_FILESYSTEMS, fileSystem))
                        continue;

                    final File file = new File(path);
                    // skip volumes that are not accessible
                    if (!file.canRead() || !file.canExecute())
                        continue;

                    List<StorageVolume> volumes = deviceVolumeMap.get(device);
                    if (volumes == null) {
                        volumes = new ArrayList<>(3);
                        deviceVolumeMap.put(device, volumes);
                    }

                    final StorageVolume volume = new StorageVolume(device, file, fileSystem);
                    final StringTokenizer flags = new StringTokenizer(tokens.nextToken(), ",");
                    label:
                    while (flags.hasMoreTokens()) {
                        final String token = flags.nextToken();
                        switch (token) {
                            case "rw":
                                volume.mReadOnly = false;
                                break label;
                            case "ro":
                                volume.mReadOnly = true;
                                break label;
                        }
                    }
                    volumes.add(volume);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        // ignored
                    }
                }
            }
        }

        // remove volumes that are the same devices
        boolean primaryStorageIncluded = false;
        final File externalStorage = Environment.getExternalStorageDirectory();
        final List<StorageVolume> volumeList = new ArrayList<>();
        for (final Entry<String, List<StorageVolume>> entry : deviceVolumeMap.entrySet()) {
            final List<StorageVolume> volumes = entry.getValue();
            if (volumes.size() == 1) {
                // go ahead and add
                final StorageVolume v = volumes.get(0);
                final boolean isPrimaryStorage = v.file.equals(externalStorage);
                primaryStorageIncluded |= isPrimaryStorage;
                setTypeAndAdd(volumeList, v, includeUsb, isPrimaryStorage);
                continue;
            }
            final int volumesLength = volumes.size();
            for (int i = 0; i < volumesLength; i++) {
                final StorageVolume v = volumes.get(i);
                if (v.file.equals(externalStorage)) {
                    primaryStorageIncluded = true;
                    // add as external storage and continue
                    setTypeAndAdd(volumeList, v, includeUsb, true);
                    break;
                }
                // if that was the last one and it's not the default external
                // storage then add it as is
                if (i == volumesLength - 1) {
                    setTypeAndAdd(volumeList, v, includeUsb, false);
                }
            }
        }
        // add primary storage if it was not found
        if (!primaryStorageIncluded) {
            final StorageVolume defaultExternalStorage = new StorageVolume("", externalStorage, "UNKNOWN");
            defaultExternalStorage.mEmulated = Environment.isExternalStorageEmulated();
            defaultExternalStorage.mType = defaultExternalStorage.mEmulated ? StorageVolume.Type.INTERNAL
                    : StorageVolume.Type.EXTERNAL;
            defaultExternalStorage.mRemovable = Environment.isExternalStorageRemovable();
            defaultExternalStorage.mReadOnly = Environment.getExternalStorageState()
                    .equals(Environment.MEDIA_MOUNTED_READ_ONLY);
            volumeList.add(0, defaultExternalStorage);
        }
        return volumeList;
    }

    public  StorageVolume getStorage(StorageVolume.Type type) {
        List<StorageVolume> list_second = getStorages(false);
        for (StorageVolume storageVolume : list_second) {
            if (storageVolume.mType == type) {
                boolean e = storageVolume.file != null && storageVolume.file.exists()
                        && storageVolume.file.list() != null;
                if (e) return storageVolume;
            }
        }
        return null;
    }

    private  void setTypeAndAdd(final List<StorageVolume> volumeList, final StorageVolume v,
                                final boolean includeUsb, final boolean asFirstItem) {
        final StorageVolume.Type type = resolveType(v);
        if (includeUsb || type != StorageVolume.Type.USB) {
            v.mType = type;
            v.mRemovable = v.file.equals(Environment.getExternalStorageDirectory()) ? Environment.isExternalStorageRemovable() : type != StorageVolume.Type.INTERNAL;

            v.mEmulated = type == StorageVolume.Type.INTERNAL;

            if (asFirstItem) volumeList.add(0, v);
            else volumeList.add(v);
        }
    }

    private  StorageVolume.Type resolveType(final StorageVolume v) {
        if (v.file.equals(Environment.getExternalStorageDirectory()) && Environment.isExternalStorageEmulated())
            return StorageVolume.Type.INTERNAL;
        else if (v.file.getAbsolutePath().equalsIgnoreCase("usb"))
            return StorageVolume.Type.USB;
        else
            return StorageVolume.Type.EXTERNAL;
    }

    private <T> boolean arrayContains(T[] array, T object) {
        for (final T item : array)
            if (item.equals(object))
                return true;
        return false;
    }

    private boolean pathContainsDir(String path, String[] dirs) {
        final StringTokenizer tokens = new StringTokenizer(path, File.separator);
        while (tokens.hasMoreElements()) {
            String next = tokens.nextToken();
            for (String dir : dirs)
                if (next.equals(dir))
                    return true;
        }
        return false;
    }

    public static final class StorageVolume {

        /**
         * Represents {@link StorageVolume} type.
         */
        public enum Type {
            INTERNAL,
            EXTERNAL,
            USB
        }

        /** Device name. */
        public final String device;

        /** Points to mount point of this device. */
        public final File file;

        /** File system of this device. */
        public final String fileSystem;

        /** if true, the storage is mounted as read-only. */
        public boolean mReadOnly;

        /** If true, the storage is removable. */
        public boolean mRemovable;

        /** If true, the storage is emulated. */
        public boolean mEmulated;

        /** Type of this storage. */
        public Type mType;

        StorageVolume(String device, File file, String fileSystem) {
            this.device = device;
            this.file = file;
            this.fileSystem = fileSystem;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((file == null) ? 0 : file.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StorageVolume other = (StorageVolume) obj;
            if (file == null) {
                return other.file == null;
            }
            return file.equals(other.file);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s%s%s%s%s%s", file.getAbsolutePath(), mReadOnly ? " ro " : " rw ", mType, mRemovable ? " R " : "", mEmulated ? " E " : "", fileSystem);
        }
    }
}

