package com.esp.android.alarmclock;

import java.util.Calendar;
import java.util.Locale;

/**
 * 休日チェック用クラス
 */
public class Holiday {

    /**
     * 休日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 休日の場合はtrue
     */
    public static boolean isHoliday(int year, int month, int day) {

        // 日曜日
        if (isSunday(year, month, day)) {
            return true;
        }

        // 祝日のチェックは日本のみ
        if (!Locale.JAPAN.equals(Locale.getDefault())) {
            return false;
        }

        // 元旦
        if (isNewYearDay(year, month, day)) {
            return true;
        }

        // 成人の日
        if (isComingOfAgeDay(year, month, day)) {
            return true;
        }

        // 建国記念日
        if (isNatinalFoundation(year, month, day)) {
            return true;
        }

        // 春分の日
        if (isSpringEquinox(year, month, day)) {
            return true;
        }

        // 昭和の日
        if (isShowaDay(year, month, day)) {
            return true;
        }

        // 憲法記念日
        if (isKenpoukikenDay(year, month, day)) {
            return true;
        }

        // みどりの日
        if (isMidoriDay(year, month, day)) {
            return true;
        }

        // こどもの日
        if (isKodomoDay(year, month, day)) {
            return true;
        }

        // 海の日
        if (isSeaDay(year, month, day)) {
            return true;
        }

        // 敬老の日
        if (isRespectForAgeDay(year, month, day)) {
            return true;
        }

        // 秋分の日
        if (isAutumnEquinox(year, month, day)) {
            return true;
        }

        // 国民の休日
        if (isNatinalHoliday(year, month, day)) {
            return true;
        }

        // 体育の日
        if (isHealthSportsDay(year, month, day)) {
            return true;
        }

        // 文化の日
        if (isCultureDay(year, month, day)) {
            return true;
        }

        // 勤労感謝の日
        if (isLaborThanksDay(year, month, day)) {
            return true;
        }

        // 天皇誕生日
        if (isTennoBirthDay(year, month, day)) {
            return true;
        }

        return false;
    }

    /**
     * 元旦チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 元旦の場合はtrue
     */
    private static boolean isNewYearDay(int year, int month, int day) {

        if (month == 1) { // １月
            if (day == 1) { // １日
                return true;
            }
            if (day == 2) { // 振替休日
                if (isSunday(year, month, 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 成人の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 成人の日の場合はtrue
     */
    private static boolean isComingOfAgeDay(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        int dayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (month == 1) { // １月
            if (dayOfWeekInMonth == 2) { // 第２
                if (dayOfWeek == Calendar.MONDAY) { // 月曜日
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 建国記念日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 建国記念日の場合はtrue
     */
    private static boolean isNatinalFoundation(int year, int month, int day) {

        if (month == 2) { // ２月
            if (day == 11) { // １１日
                return true;
            }
            if (day == 12) { // 振替休日
                if (isSunday(year, month, 11)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 春分の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 春分の日の場合はtrue
     */
    private static boolean isSpringEquinox(int year, int month, int day) {

        if (year > 1979 && year < 2100) { // 1980～2100年まで有効
            if (month == 3) { // ３月
                int daySpringEquinox = (int)(20.8431+(0.242194*(year-1980))-((year-1980)/4));
                if (day == daySpringEquinox) {
                    return true;
                }
                if (day == daySpringEquinox+1) { // 振替休日
                    if (isSunday(year, month, daySpringEquinox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 昭和の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 昭和の日の場合はtrue
     */
    private static boolean isShowaDay(int year, int month, int day) {

        if (month == 4) { // ４月
            if (day == 29) { // ２９日
                return true;
            }
            if (day == 30) { // 振替休日
                if (isSunday(year, month, 29)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 憲法記念日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 憲法記念日の場合はtrue
     */
    private static boolean isKenpoukikenDay(int year, int month, int day) {

        if (month == 5) { // ５月
            if (day == 3) { // ３日
                return true;
            }
            if (day == 6) { // 振替休日（６日）
                if (isSunday(year, month, 3)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * みどりの日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return みどりの日の場合はtrue
     */
    private static boolean isMidoriDay(int year, int month, int day) {

        if (month == 5) { // ５月
            if (day == 4) { // ４日
                return true;
            }
            if (day == 6) { // 振替休日（６日）
                if (isSunday(year, month, 4)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * こどもの日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return こどもの日の場合はtrue
     */
    private static boolean isKodomoDay(int year, int month, int day) {

        if (month == 5) { // ５月
            if (day == 5) { // ５日
                return true;
            }
            if (day == 6) { // 振替休日
                if (isSunday(year, month, 5)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 海の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 海の日の場合はtrue
     */
    private static boolean isSeaDay(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        int dayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (month == 7) { // ７月
            if (dayOfWeekInMonth == 3) { // 第３
                if (dayOfWeek == Calendar.MONDAY) { // 月曜日
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 敬老の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 敬老の日の場合はtrue
     */
    private static boolean isRespectForAgeDay(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        int dayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (month == 9) { // ９月
            if (dayOfWeekInMonth == 3) { // 第３
                if (dayOfWeek == Calendar.MONDAY) { // 月曜日
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 秋分の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 秋分の日の場合はtrue
     */
    private static boolean isAutumnEquinox(int year, int month, int day) {

        if (year > 1979 && year < 2100) { // 1980～2100年まで有効
            if (month == 9) { // ９月
                int dayAutumnEquinox = (int)(23.2488+(0.242194*(year-1980))-((year-1980)/4));
                if (day == dayAutumnEquinox) {
                    return true;
                }
                if (day == dayAutumnEquinox+1) { // 振替休日
                    if (isSunday(year, month, dayAutumnEquinox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 国民の休日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 国民の休日の場合はtrue
     */
    private static boolean isNatinalHoliday(int year, int month, int day) {
        // 敬老の日と秋分の日に挟まれた日
        if (isRespectForAgeDay(year, month, day-1) && isAutumnEquinox(year, month, day+1)) {
            return true;
        }
        return false;
    }

    /**
     * 体育の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 体育の日の場合はtrue
     */
    private static boolean isHealthSportsDay(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        int dayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (month == 10) { // １０月
            if (dayOfWeekInMonth == 2) { // 第２
                if (dayOfWeek == Calendar.MONDAY) { // 月曜日
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 文化の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 文化の日の場合はtrue
     */
    private static boolean isCultureDay(int year, int month, int day) {

        if (month == 11) { // １１月
            if (day == 3) { // ３日
                return true;
            }
            if (day == 4) { // 振替休日
                if (isSunday(year, month, 3)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 勤労感謝の日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 勤労感謝の日の場合はtrue
     */
    private static boolean isLaborThanksDay(int year, int month, int day) {

        if (month == 11) { // １１月
            if (day == 23) { // ２３日
                return true;
            }
            if (day == 24) { // 振替休日
                if (isSunday(year, month, 23)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 天皇誕生日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 天皇誕生日の場合はtrue
     */
    private static boolean isTennoBirthDay(int year, int month, int day) {

        if (month == 12) { // １２月
            if (day == 23) { // ２３日
                return true;
            }
            if (day == 24) { // 振替休日
                if (isSunday(year, month, 23)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 日曜日チェック
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 日曜日の場合はtrue
     */
    private static boolean isSunday(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) {
            return true;
        }
        return false;
    }
}
