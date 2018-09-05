package cn.atuan.calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.atuan.datepickerlibrary.CalendarUtil;
import com.atuan.datepickerlibrary.DatePopupWindow;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private int startGroup = -1;
    private int endGroup = -1;
    private int startChild = -1;
    private int endChild = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        TextView date = (TextView) findViewById(R.id.btn_date);
        TextView time = (TextView) findViewById(R.id.btn_time);
        final TextView result = (TextView) findViewById(R.id.result);
        TextView customDate = (TextView) findViewById(R.id.btn_customDate);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(MainActivity.this, R.style.DialogDateTheme, result, Calendar.getInstance());
            }
        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(MainActivity.this, R.style.DialogDateTheme, result, Calendar.getInstance());
            }
        });
        customDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCustomDatePicker(view, result);
            }
        });
    }

    /**
     * 日期选择
     *
     * @param activity
     * @param themeResId
     * @param result
     * @param calendar
     */
    public void showDatePickerDialog(Activity activity, int themeResId, final TextView result, Calendar calendar) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                result.setText("您选择了：" + year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日");
            }
        }       // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * 时间选择
     *
     * @param activity
     * @param themeResId
     * @param result
     * @param calendar
     */
    public void showTimePickerDialog(Activity activity, int themeResId, final TextView result, Calendar calendar) {
        // 创建一个TimePickerDialog实例，并把它显示出来
        new TimePickerDialog(activity, themeResId,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        result.setText("您选择了：" + hourOfDay + "时" + minute + "分");
                    }
                }
                // 设置初始时间
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                , true).show();
    }

    private void createCustomDatePicker(View view, final TextView result) {
        new DatePopupWindow
                .Builder(MainActivity.this, Calendar.getInstance().getTime(), view)
                .setInitSelect(startGroup, startChild, endGroup, endChild)
                .setInitDay(false)
                .setDateOnClickListener(new DatePopupWindow.DateOnClickListener() {
                    @Override
                    public void getDate(String startDate, String endDate, int startGroupPosition, int startChildPosition, int endGroupPosition, int endChildPosition) {
                        startGroup = startGroupPosition;
                        startChild = startChildPosition;
                        endGroup = endGroupPosition;
                        endChild = endChildPosition;
                        String mStartTime = CalendarUtil.FormatDateYMD(startDate);
                        String mEndTime = CalendarUtil.FormatDateYMD(endDate);
                        result.setText("您选择了：" + mStartTime + "到" + mEndTime);
                    }
                }).builder();
    }
}
