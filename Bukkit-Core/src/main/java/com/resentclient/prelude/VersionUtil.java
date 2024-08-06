/*
 * Prelude-API is a plugin to implement features for the Client.
 * Copyright (C) 2024 cire3, Preva1l
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.resentclient.prelude;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


// --------------- BEGIN COPYING FROM ESSENTIALS ----------------

@SuppressWarnings("unused")
public final class VersionUtil {

    public static final BukkitVersion v1_14_R01 = BukkitVersion.fromString("1.14-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_8_8_R01 = BukkitVersion.fromString("1.8.8-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_9_R01 = BukkitVersion.fromString("1.9-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_9_4_R01 = BukkitVersion.fromString("1.9.4-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_10_2_R01 = BukkitVersion.fromString("1.10.2-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_11_R01 = BukkitVersion.fromString("1.11-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_11_2_R01 = BukkitVersion.fromString("1.11.2-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_12_0_R01 = BukkitVersion.fromString("1.12.0-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_12_2_R01 = BukkitVersion.fromString("1.12.2-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_13_0_R01 = BukkitVersion.fromString("1.13.0-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_13_2_R01 = BukkitVersion.fromString("1.13.2-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_14_4_R01 = BukkitVersion.fromString("1.14.4-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_15_R01 = BukkitVersion.fromString("1.15-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_15_2_R01 = BukkitVersion.fromString("1.15.2-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_16_1_R01 = BukkitVersion.fromString("1.16.1-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_16_5_R01 = BukkitVersion.fromString("1.16.5-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_17_R01 = BukkitVersion.fromString("1.17-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_17_1_R01 = BukkitVersion.fromString("1.17.1-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_18_2_R01 = BukkitVersion.fromString("1.18.2-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_19_R01 = BukkitVersion.fromString("1.19-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_19_4_R01 = BukkitVersion.fromString("1.19.4-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_20_1_R01 = BukkitVersion.fromString("1.20.1-R0.1-SNAPSHOT");
    public static final BukkitVersion v1_20_4_R01 = BukkitVersion.fromString("1.20.4-R0.1-SNAPSHOT");

    private static BukkitVersion serverVersion = null;

    private VersionUtil() {
    }

    public static BukkitVersion getServerBukkitVersion() {
        if (serverVersion == null) {
            serverVersion = BukkitVersion.fromString(Bukkit.getServer().getBukkitVersion());
        }
        return serverVersion;
    }

    public static final class BukkitVersion implements Comparable<BukkitVersion> {
        private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.?([0-9]*)?(?:-pre(\\d))?(?:-rc(\\d+))?(?:-?R?([\\d.]+))?(?:-SNAPSHOT)?");

        private final int major;
        private final int minor;
        private final int preRelease;
        private final int releaseCandidate;
        private final int patch;
        private final double revision;
        private final boolean unknown;
        private final boolean testVersion;

        private BukkitVersion(final int major, final int minor, final int patch, final double revision, final int preRelease, final int releaseCandidate) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.revision = revision;
            this.preRelease = preRelease;
            this.releaseCandidate = releaseCandidate;
            this.unknown = false;
            this.testVersion = revision == 0.0;
        }

        private BukkitVersion() {
            this.major = 0;
            this.minor = 0;
            this.patch = 0;
            this.revision = 0;
            this.preRelease = 0;
            this.releaseCandidate = 0;
            this.unknown = true;
            this.testVersion = false;
        }

        public static BukkitVersion unknown() {
            return new BukkitVersion();
        }

        public static BukkitVersion fromString(final String string) {
            Preconditions.checkNotNull(string, "string cannot be null.");
            Matcher matcher = VERSION_PATTERN.matcher(string);
            if (!matcher.matches()) {
                return BukkitVersion.unknown();
            }

            return from(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(6), matcher.group(4), matcher.group(5));
        }

        private static BukkitVersion from(final String major, final String minor, String patch, String revision, String preRelease, String releaseCandidate) {
            if (patch == null || patch.isEmpty()) patch = "0";
            if (revision == null || revision.isEmpty()) revision = "0";
            if (preRelease == null || preRelease.isEmpty()) preRelease = "-1";
            if (releaseCandidate == null || releaseCandidate.isEmpty()) releaseCandidate = "-1";
            return new BukkitVersion(Integer.parseInt(major),
                    Integer.parseInt(minor),
                    Integer.parseInt(patch),
                    Double.parseDouble(revision),
                    Integer.parseInt(preRelease),
                    Integer.parseInt(releaseCandidate));
        }

        public boolean isHigherThan(final BukkitVersion o) {
            return compareTo(o) > 0;
        }

        public boolean isHigherThanOrEqualTo(final BukkitVersion o) {
            return compareTo(o) >= 0;
        }

        public boolean isLowerThan(final BukkitVersion o) {
            return compareTo(o) < 0;
        }

        public boolean isLowerThanOrEqualTo(final BukkitVersion o) {
            return compareTo(o) <= 0;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getPatch() {
            return patch;
        }

        public double getRevision() {
            return revision;
        }

        public int getPrerelease() {
            return preRelease;
        }

        public int getReleaseCandidate() {
            return releaseCandidate;
        }

        public boolean isUnknown() {
            return unknown;
        }

        public boolean isTestVersion() {
            return testVersion;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final BukkitVersion that = (BukkitVersion) o;
            return major == that.major &&
                    minor == that.minor &&
                    patch == that.patch &&
                    revision == that.revision &&
                    preRelease == that.preRelease;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(major, minor, patch, revision, preRelease, releaseCandidate);
        }

        @Override
        public String toString() {
            if (this.isUnknown()) {
                return "Unknown";
            }
            final StringBuilder sb = new StringBuilder(major + "." + minor);
            if (patch != 0) {
                sb.append(".").append(patch);
            }
            if (preRelease != -1) {
                sb.append("-pre").append(preRelease);
            }
            if (releaseCandidate != -1) {
                sb.append("-rc").append(releaseCandidate);
            }
            return sb.append("-R").append(revision).toString();
        }

        @Override
        public int compareTo(final BukkitVersion o) {
            if (major < o.major) {
                return -1;
            } else if (major > o.major) {
                return 1;
            } else { // equal major
                if (minor < o.minor) {
                    return -1;
                } else if (minor > o.minor) {
                    return 1;
                } else { // equal minor
                    if (patch < o.patch) {
                        return -1;
                    } else if (patch > o.patch) {
                        return 1;
                    } else { // equal patch
                        if (preRelease < o.preRelease) {
                            return -1;
                        } else if (preRelease > o.preRelease) {
                            return 1;
                        } else { // equal prerelease
                            if (releaseCandidate < o.releaseCandidate) {
                                return -1;
                            } else if (releaseCandidate > o.releaseCandidate) {
                                return 1;
                            } else { // equal release candidate
                                return Double.compare(revision, o.revision);
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------- END COPYING FROM ESSENTIALS ----------------