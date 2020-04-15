package rocks.tbog.tblauncher.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Process;

import androidx.annotation.RequiresApi;


/**
 * Wrapper class for `android.os.UserHandle` that works with all Android versions
 */
public class UserHandle {
    private final long serial;
    private final Object handle; // android.os.UserHandle on Android 4.2 and newer

    public UserHandle() {
        this(0, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public UserHandle(long serial, android.os.UserHandle user) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // OS does not provide any APIs for multi-user support
            this.serial = 0;
            this.handle = null;
        } else if (user != null && Process.myUserHandle().equals(user)) {
            // For easier processing the current user is also stored as `null`, even
            // if there is multi-user support
            this.serial = 0;
            this.handle = null;
        } else {
            // Store the given user handle
            this.serial = serial;
            this.handle = user;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public android.os.UserHandle getRealHandle() {
        if (this.handle != null) {
            return (android.os.UserHandle) this.handle;
        } else {
            return Process.myUserHandle();
        }
    }


    public boolean isCurrentUser() {
        return (this.handle == null);
    }


    private String addUserSuffixToString(String base, char separator) {
        if (this.handle == null) {
            return base;
        } else {
            return base + separator + this.serial;
        }
    }

    @SuppressWarnings("CatchAndPrintStackTrace")
    public boolean hasStringUserSuffix(String string, char separator) {
        long serial = 0;

        int index = string.lastIndexOf((int) separator);
        if (index > -1) {
            String serialText = string.substring(index);
            try {
                serial = Long.parseLong(serialText);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return (serial == this.serial);
    }

    public String getComponentName(String packageName, String activityName) {
        return addUserSuffixToString(packageName + "/" + activityName, '#');
    }

    public String getPackageName(String componentName) {
        int index = componentName.indexOf('/');
        if (index > 0)
            return componentName.substring(0, index);
        return "";
    }

    public String getActivityName(String componentName) {
        int start = componentName.indexOf('/') + 1;
        int end = componentName.lastIndexOf('#');
        if (end == -1)
            end = componentName.length();
        if (start > 0 && start < end) {
            return componentName.substring(start, end);
        }
        return "";
    }

    public String getBadgedLabelForUser(Context context, String label) {
        if (handle == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return label;
        return context.getPackageManager().getUserBadgedLabel(label, (android.os.UserHandle) handle).toString();
    }
}
