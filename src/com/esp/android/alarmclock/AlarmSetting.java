package com.esp.android.alarmclock;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;

/**
 * アラーム毎の設定値をあらわすクラス
 */
public class AlarmSetting {

    // ID
    long id;

    // ON/OFF
    int onOff;

    // アラーム名
    String title;

    // 繰返し
    enum Repeat {
        Daily,       // 毎日
        SpecifyDay,  // 曜日指定
        Weekday,     // 平日（休日除く）
        DayOff,      // 日曜/休日
        OnceOnly;    // 1回のみ
    };
    Repeat repeat;

    // 曜日
    enum Week {
        Sunday(Calendar.SUNDAY),        // 日曜日
        Monday(Calendar.MONDAY),        // 月曜日
        Tuesday(Calendar.TUESDAY),      // 火曜日
        Wednesday(Calendar.WEDNESDAY),  // 水曜日
        Thursday(Calendar.THURSDAY),    // 木曜日
        Friday(Calendar.FRIDAY),        // 金曜日
        Saturday(Calendar.SATURDAY);    // 土曜日
        private int value;
        private Week(int value) {
            this.value = value;
        }
        public int getValue() {
            return this.value;
        }
        public static Week valueOf(int value) {
            for (Week w : values()) {
                if (w.getValue() == value) {
                    return w;
                }
            }
            return null;
        }
    };
    EnumSet<Week> week;

    // 年月日
    public class Ymd {
        int year;   // 年
        int month;  // 月
        int day;    // 日
        public Ymd(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }
    Ymd ymd;

    // 時
    int hour;

    // 分
    int minute;

    // 音楽のキー
    MusicKey musicKey;

    // 音楽のボリューム（％）
    int musicVolume;

    // 鳴動時間（秒）
    int musicLength;

    // 音声
    int voice;

    // バイブレータ
    int vibrator;

    // スヌーズモード
    enum SnoozeMode {
        SnoozeOff,       // OFF
        SnoozeOn,        // ON
        VolumeUp;  // ボリュームアップ
    };
    SnoozeMode snoozeMode;

    // スヌーズ間隔（分）
    int snoozeLength;

    // スヌーズ回数
    int snoozeTimes;

    /**
     * コンストラクタ
     */
    public AlarmSetting() {
        this.id = 0;
        this.onOff = 0;
        this.title = "";
        this.repeat = Repeat.Daily;
        this.week = EnumSet.allOf(Week.class);
        this.ymd = new Ymd(0,0,0);
        this.hour = 0;
        this.minute = 0;
        this.musicKey = new MusicKey();
        this.musicVolume = 100;
        this.musicLength = 300;
        this.voice = 0;
        this.vibrator = 0;
        this.snoozeMode = SnoozeMode.SnoozeOff;
        this.snoozeLength = 10;
        this.snoozeTimes = 5;
    }
    

    /**
     * 次のアラーム日時を取得
     * @return GregorianCalendar
     */
    public GregorianCalendar getNextCalendar() {
        // 現在日時を取得
        GregorianCalendar nowCalendar = new GregorianCalendar();

        // 時刻を設定
        GregorianCalendar nextCalendar = new GregorianCalendar();
        nextCalendar.set(Calendar.HOUR_OF_DAY, hour);
        nextCalendar.set(Calendar.MINUTE, minute);
        nextCalendar.set(Calendar.SECOND, 0);
        nextCalendar.set(Calendar.MILLISECOND, 0);

        // 毎日
        if (repeat == Repeat.Daily) {
            // 現在日時より後でなければ翌日にする
            if (!nextCalendar.after(nowCalendar)) {
                nextCalendar.add(Calendar.DATE, 1);
            }

        // 曜日指定
        } else if (repeat == Repeat.SpecifyDay) {
            // 最大８日分チェック
            boolean check=false;
            for (int i=0; i<8; i++) {
                int dayOfWeek = nextCalendar.get(Calendar.DAY_OF_WEEK);
                if (week.contains(Week.valueOf(dayOfWeek))) {
                    // 現在日時より後であること
                    if (nextCalendar.after(nowCalendar)) {
                        check = true;
                        break;
                    }
                }
                nextCalendar.add(Calendar.DATE, 1);
            }
            // チェックできなければnull
            if (!check) {
                return null;
            }

        // 平日（休日除く）
        } else if (repeat == Repeat.Weekday) {
            // 最大36日分チェック
            boolean check=false;
            for (int i=0; i<36; i++) {
                int dayOfWeek = nextCalendar.get(Calendar.DAY_OF_WEEK);
                if (week.contains(Week.valueOf(dayOfWeek))) {
                    int year = nextCalendar.get(Calendar.YEAR);
                    int month = nextCalendar.get(Calendar.MONTH)+1;
                    int day = nextCalendar.get(Calendar.DATE);
                    if (!Holiday.isHoliday(year, month, day)) {
                        // 現在日時より後であること
                        if (nextCalendar.after(nowCalendar)) {
                            check = true;
                            break;
                        }
                    }
                }
                nextCalendar.add(Calendar.DATE, 1);
            }
            // チェックできなければnull
            if (!check) {
                return null;
            }

        // 日曜/休日
        } else if (repeat == Repeat.DayOff) {
            // 見つかるまでチェック
            while (true) {
                int year = nextCalendar.get(Calendar.YEAR);
                int month = nextCalendar.get(Calendar.MONTH)+1;
                int day = nextCalendar.get(Calendar.DATE);
                if (Holiday.isHoliday(year, month, day)) {
                    // 現在日時より後であること
                    if (nextCalendar.after(nowCalendar)) {
                        break;
                    }
                }
                nextCalendar.add(Calendar.DATE, 1);
            }

        // 1回のみ
        } else if (repeat == Repeat.OnceOnly) {
            // 年月日を設定
            nextCalendar.set(Calendar.YEAR, ymd.year);
            nextCalendar.set(Calendar.MONTH, ymd.month);
            nextCalendar.set(Calendar.DATE, ymd.day);

            // 現在日時より後でなければnull
            if (!nextCalendar.after(nowCalendar)) {
                return null;
            }
        }

        return nextCalendar;
    }

    /**
     * booleanをintに変換する
     * @param param boolean
     * @return int
     */
    public static int booleanToInt(boolean param) {
        if (param) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * intをbooleanに変換する
     * @param param int
     * @return boolean
     */
    public static boolean intToBoolean(int param) {
        if (param == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * EnumSet<Week>をintに変換する
     * @param weekSet EnumSet<Week>
     * @return int
     */
    public static int weekSetToInt(EnumSet<Week> weekSet) {
        return enumSetToInt(weekSet);
    }

    /**
     * intをEnumSet<Week>に変換する
     * @param param int
     * @return EnumSet<Week>
     */
    public static EnumSet<Week> intToWeekSet(int param) {
        return intToEnumSet(Week.class, Week.values(), param);
    }

    /**
     * EnumSet<T>をintに変換する
     * @param enumSet EnumSet<T>
     * @return int
     */
    private static <T extends Enum<T>> int enumSetToInt(EnumSet<T> enumSet) {
        int i = 0;
        for (T o : enumSet) {
            i += Math.pow(2, o.ordinal());
        }
        return i;
    }

    /**
     * intをEnumSet<T>に変換する
     * @param tclass 列挙名.class
     * @param values 列挙名.values()
     * @param param int
     * @return EnumSet<T>
     */
    private static <T extends Enum<T>> EnumSet<T> intToEnumSet(Class<T> tclass,
            T[] values, int param) {
        EnumSet<T> enumSet = EnumSet.noneOf(tclass);
        for (int i = 0; i < values.length; i++) {
            if ((param & (int)Math.pow(2, i)) != 0) {
                enumSet.add(values[i]);
            }
        }
        return enumSet;
    }
}
