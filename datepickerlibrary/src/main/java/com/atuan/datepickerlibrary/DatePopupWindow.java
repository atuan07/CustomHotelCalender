package com.atuan.datepickerlibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by atuan on 2018/9/1.
 */
public class DatePopupWindow extends PopupWindow {

    private View rootView;
    private TextView tvOk;
    private RecyclerView rv;
    private TextView tvStartDate;
    private TextView tvStartWeek;
    private TextView tvEndDate;
    private TextView tvEndWeek;
    private TextView tvTime;
    private LinearLayout llEnd;
    private TextView tvHintText;
    private TextView btnClose;
    private TextView btnClear;
    private Activity activity;
    private Date mSetDate;
    private String currentDate;
    private int startGroupPosition = -1;
    private int endGroupPosition = -1;
    private int startChildPosition = -1;
    private int endChildPosition = -1;
    private int c_stratChildPosition = -1;//当天在列表中的子索引
    private DateAdapter mDateAdapter;
    private List<DateInfo> mList;
    private DateOnClickListener mOnClickListener = null;


    private DatePopupWindow(Builder builder) {

        this.activity = builder.context;
        this.currentDate = builder.date;
        this.startGroupPosition = builder.startGroupPosition;
        this.startChildPosition = builder.startChildPosition;
        this.endGroupPosition = builder.endGroupPosition;
        this.endChildPosition = builder.endChildPosition;
        this.mOnClickListener = builder.mOnClickListener;

        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.popupwindow_hotel_date, null);
        this.setContentView(rootView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setAnimationStyle(R.style.dialogWindowAnim);
        this.setFocusable(true);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setOnDismissListener(new ShareDismissListener());
        backgroundAlpha(activity, 0.5f);

        initView();
        setInitSelect();
        create(builder.parentView);
    }

    private void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        context.getWindow().setAttributes(lp);
    }

    private void initView() {
        tvOk = (TextView) rootView.findViewById(R.id.tv_ok);
        btnClose = (TextView) rootView.findViewById(R.id.btn_close);
        btnClear = (TextView) rootView.findViewById(R.id.btn_clear);
        tvStartDate = (TextView) rootView.findViewById(R.id.tv_startDate);
        tvStartWeek = (TextView) rootView.findViewById(R.id.tv_startWeek);
        tvEndDate = (TextView) rootView.findViewById(R.id.tv_endDate);
        tvEndWeek = (TextView) rootView.findViewById(R.id.tv_endWeek);
        tvTime = (TextView) rootView.findViewById(R.id.tv_time);
        llEnd = (LinearLayout) rootView.findViewById(R.id.ll_end);
        tvHintText = (TextView) rootView.findViewById(R.id.tv_hintText);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    String startDate = mList.get(startGroupPosition)
                            .getList()
                            .get(startChildPosition)
                            .getDate();
                    String endDate = mList.get(endGroupPosition)
                            .getList()
                            .get(endChildPosition)
                            .getDate();
                    mOnClickListener.getDate(startDate, endDate, startGroupPosition, startChildPosition, endGroupPosition, endChildPosition);
                }
                DatePopupWindow.this.dismiss();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePopupWindow.this.dismiss();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //重置操作
                initView();//重置当前view
                setDefaultSelect();//选中初始状态值
            }
        });

        ((DefaultItemAnimator) rv.getItemAnimator()).setSupportsChangeAnimations(false);//关闭动画
        LinearLayoutManager manager = new LinearLayoutManager(activity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        mList = new ArrayList<>();
        mDateAdapter = new DateAdapter(mList);
        rv.setAdapter(mDateAdapter);
        initData();
    }

    @SuppressLint("SimpleDateFormat")
    private void initData() {
        SimpleDateFormat ymd_sdf = new SimpleDateFormat("yyyy-MM-dd");//当前日期转date
        try {
            if (currentDate == null) {
                new Throwable("please set one start time");
                return;
            }
            mSetDate = ymd_sdf.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //初始化日期
        Calendar c = Calendar.getInstance();
        c.setTime(mSetDate);
        int firstM = c.get(Calendar.MONTH) + 1;//获取月份 月份是从0开始
        int days = c.get(Calendar.DATE);//日期
        int week = c.get(Calendar.DAY_OF_WEEK);//周几
        //获取当前这个月最大天数
        int maxDys = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        DateInfo info = new DateInfo();
        List<DayInfo> dayList = new ArrayList<>();
        info.setDate(c.get(Calendar.YEAR) + "年" + firstM + "月");
        //当小于当前日期时，是不可选，setEnable(false)
        //当前月第一天是周几
        int w = CalendarUtil.getWeekNoFormat(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-01") - 1;
        //根据该月的第一天，星期几，填充上个月的空白日期
        for (int t = 0; t < w; t++) {
            DayInfo dayInfo = new DayInfo();
            dayInfo.setName("");
            dayInfo.setEnable(false);
            dayInfo.setDate("");
            dayList.add(dayInfo);
        }
        //计算当前月的天数
        for (int i = 1; i <= maxDys; i++) {
            DayInfo dayInfo = new DayInfo();
            dayInfo.setName(i + "");
            dayInfo.setDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + i);
            int c_year = Integer.parseInt(currentDate.split("-")[0]);
            int c_month = Integer.parseInt(currentDate.split("-")[1]);
            int c_day = Integer.parseInt(currentDate.split("-")[2]);
            if (c_year == c.get(Calendar.YEAR) && c_month == (c.get(Calendar.MONTH) + 1) && c_day == i) {
                c_stratChildPosition = dayList.size();
            }
            if (i < days) {
                dayInfo.setEnable(false);
            } else {
                dayInfo.setEnable(true);
            }
            dayList.add(dayInfo);
        }
        info.setList(dayList);
        mList.add(info);
        //获取下7个月的数据
        for (int i = 1; i < 8; i++) {
            //当前月份循环加1
            c.add(Calendar.MONTH, 01);
            DateInfo nextInfo = new DateInfo();
            List<DayInfo> nextdayList = new ArrayList<>();
            int maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            nextInfo.setDate(c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月");
            //周几
            int weeks = CalendarUtil.getWeekNoFormat(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-01") - 1;
            //根据该月的第一天，星期几，填充上个月的空白日期
            for (int t = 0; t < weeks; t++) {
                DayInfo dayInfo = new DayInfo();
                dayInfo.setName("");
                dayInfo.setEnable(false);
                dayInfo.setDate("");
                nextdayList.add(dayInfo);
            }
            //该月的所有日期
            for (int j = 0; j < maxDays; j++) {
                DayInfo dayInfo = new DayInfo();
                dayInfo.setName((j + 1) + "");
                dayInfo.setEnable(true);
                dayInfo.setDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + (j + 1));
                nextdayList.add(dayInfo);
            }
            nextInfo.setList(nextdayList);
            mList.add(nextInfo);
        }
        mDateAdapter.updateData();
    }

    private void setInitSelect() {
        if (0 <= this.startGroupPosition && this.startGroupPosition < mList.size() && 0 <= this.endGroupPosition && this.endGroupPosition < mList.size()) {
            int maxEndChild = mList.get(this.endGroupPosition).getList().size();
            int maxStartChild = mList.get(this.startGroupPosition).getList().size();
            if (0 <= this.startChildPosition && this.startChildPosition < maxStartChild && 0 <= this.endChildPosition && this.endChildPosition < maxEndChild) {
                setInit();
            } else {
                setDefaultSelect();//设置根据mDate设定今天和明天日期
            }
        } else {
            setDefaultSelect();//设置根据mDate设定今天和明天日期
        }
    }

    private void setInit() {
        mList.get(this.startGroupPosition).getList().get(this.startChildPosition).setStatus(1);
        mList.get(this.endGroupPosition).getList().get(this.endChildPosition).setStatus(2);
        mDateAdapter.notifyDataSetChanged();
        getoffsetDate(mList.get(startGroupPosition).getList().get(startChildPosition).getDate(),
                mList.get(endGroupPosition).getList().get(endChildPosition).getDate(), true);
        rv.scrollToPosition(this.startGroupPosition);
    }

    //设置日历标明当前日期的状态
    @SuppressLint("SimpleDateFormat")
    private void setDefaultSelect() {
        if (c_stratChildPosition == -1) return;
        String date = mList.get(0).getList().get(c_stratChildPosition).getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = null;
        try {
            curDate = sdf.parse(FormatDate(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (curDate == null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.add(Calendar.DATE, 1);

        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH) + 1
                && c_stratChildPosition < mList.get(0).getList().size() - 1) {
            this.startGroupPosition = 0;
            this.startChildPosition = c_stratChildPosition;
            this.endGroupPosition = 0;
            this.endChildPosition = c_stratChildPosition + 1;
            setInit();
        } else {
            for (int i = 0; i < mList.get(1).getList().size(); i++) {
                if (!TextUtils.isEmpty(mList.get(1).getList().get(i).getDate())) {
                    this.startGroupPosition = 0;
                    this.startChildPosition = c_stratChildPosition;
                    this.endGroupPosition = 1;
                    this.endChildPosition = i;
                    setInit();
                    break;
                }
            }
        }
    }

    /**
     * 设置起始时间和结束时间的选中标识，或者设置不选中
     *
     * @param startDate
     * @param endDate
     * @param status    选中设置为true 设置不选中false
     */
    @SuppressLint("SetTextI18n")
    private void getoffsetDate(String startDate, String endDate, boolean status) {

        //更新开始日期和结束日期的信息和状态
        Calendar sCalendar = CalendarUtil.toDate(startDate);
        Calendar eCalendar = CalendarUtil.toDate(endDate);
        tvStartDate.setText((sCalendar.get(Calendar.MONTH) + 1) + "月" + sCalendar.get(Calendar.DAY_OF_MONTH) + "日");
        tvStartWeek.setText("周" + CalendarUtil.getWeekByFormat(startDate));
        tvEndDate.setText((eCalendar.get(Calendar.MONTH) + 1) + "月" + eCalendar.get(Calendar.DAY_OF_MONTH) + "日");
        tvEndWeek.setText("周" + CalendarUtil.getWeekByFormat(endDate));
        int daysOffset = Integer.parseInt(CalendarUtil.getTwoDay(endDate, startDate));
        if (daysOffset < 0) return;
        tvTime.setText("共" + (daysOffset + 1) + "天");

        //更改结束日期和完成按钮状态
        llEnd.setVisibility(View.VISIBLE);
        tvHintText.setVisibility(View.GONE);
        tvOk.setText("完成");
        tvOk.setEnabled(true);
        tvOk.setBackgroundResource(R.drawable.img_btn_bg_y);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        DayInfo info = mList.get(startGroupPosition).getList().get(startChildPosition);
        try {
            c.setTime(sdf.parse(info.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //根据2个时间的相差天数去循环
        for (int i = 0; i < daysOffset; i++) {
            //下一天（目标天）
            c.add(Calendar.DATE, 1);
            //改天的日期（目标天）
            String d = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);
            //循环group列表
            for (int j = 0; j < mList.size(); j++) {
                //获取该月的随机一个dayInfo
                DayInfo dayInfo = mList.get(j).getList().get(mList.get(j).getList().size() - 1);
                boolean isCheck = false;
                //判断该天是否和目标天是否是同一个月
                if (!TextUtils.isEmpty(dayInfo.getDate()) && Integer.valueOf(dayInfo.getDate().split("-")[0]) == (c.get(Calendar.YEAR))
                        && Integer.valueOf(dayInfo.getDate().split("-")[1]) == ((c.get(Calendar.MONTH) + 1))) {
                    //是同一个月，则循环该月多有天数
                    for (int t = 0; t < mList.get(j).getList().size(); t++) {
                        //找到该月的日期与目标日期相同，存在，设置选择标记
                        if (mList.get(j).getList().get(t).getDate().equals(d)) {
                            mList.get(j).getList().get(t).setSelect(status);
                            isCheck = true;
                            break;
                        }
                    }
                }
                if (isCheck) {
                    mDateAdapter.notifyItemChanged(j);
                    break;
                }
            }
        }
    }

    private String FormatDate(String date) {
        if (TextUtils.isEmpty(date)) return "";
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(date.split("-")[0]);
        stringBuffer.append("-");
        stringBuffer.append(date.split("-")[1].length() < 2 ? "0" + date.split("-")[1] : date.split("-")[1]);
        stringBuffer.append("-");
        stringBuffer.append(date.split("-")[2].length() < 2 ? "0" + date.split("-")[2] : date.split("-")[2]);
        return stringBuffer.toString();
    }

    private void create(View view) {
        this.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * return startDate、endDate(格式：2012-12-10)
     * 选中完成后返回开始时间和结束时间
     * return startGroupPosition、startChildPosition、endGroupPosition、endChildPosition
     * 返回选中时间区间的状态标记，监听中接收后在builder中setInitSelect()方法中直接传出入（可用于记录上次选中的状态，用户再点击进入的时候恢复上一次的区间选中状态）
     */
    public interface DateOnClickListener {
        void getDate(String startDate, String endDate, int startGroupPosition, int startChildPosition, int endGroupPosition, int endChildPosition);
    }

    public static class Builder {
        private String date;
        private Activity context;
        private View parentView;
        private int startGroupPosition = -1;
        private int endGroupPosition = -1;
        private int startChildPosition = -1;
        private int endChildPosition = -1;
        private DateOnClickListener mOnClickListener = null;

        @SuppressLint("SimpleDateFormat")
        public Builder(Activity context, Date date, View parentView) {
            this.date = new SimpleDateFormat("yyyy-MM-dd").format(date);
            this.context = context;
            this.parentView = parentView;
        }

        public DatePopupWindow builder() {
            return new DatePopupWindow(this);
        }

        public Builder setInitSelect(int startGroup, int startChild, int endGroup, int endChild) {
            this.startGroupPosition = startGroup;
            this.startChildPosition = startChild;
            this.endGroupPosition = endGroup;
            this.endChildPosition = endChild;
            return this;
        }

        public Builder setDateOnClickListener(DateOnClickListener mlListener) {
            mOnClickListener = mlListener;
            return this;
        }
    }

    private class DateAdapter extends BaseQuickAdapter<DateInfo, BaseViewHolder> {

        DateAdapter(@Nullable List<DateInfo> data) {
            super(R.layout.adapter_hotel_select_date, data);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int positions) {
            super.onBindViewHolder(holder, positions);
            TextView tv = holder.getView(R.id.tv_date);
            tv.setText(mList.get(positions).getDate());
        }

        @Override
        protected void convert(final BaseViewHolder helper, final DateInfo item) {
            RecyclerView rv = helper.getView(R.id.rv_date);

            GridLayoutManager manager = new GridLayoutManager(activity, 7);
            rv.setLayoutManager(manager);
            final TempAdapter groupAdapter = new TempAdapter(item.getList());
            rv.setAdapter(groupAdapter);
            groupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (!item.getList().get(position).isEnable()) return;
                    if (TextUtils.isEmpty(item.getList().get(position).getName())) return;
                    if (TextUtils.isEmpty(item.getList().get(position).getDate())) return;
                    int status = item.getList().get(position).getStatus();
                    if (status == 0 && startGroupPosition == -1 && startChildPosition == -1 && item.getList().get(position).isEnable()) {
                        //开始
                        item.getList().get(position).setStatus(1);
                        adapter.notifyItemChanged(position);
                        startGroupPosition = helper.getAdapterPosition();
                        startChildPosition = position;
                        String mStartTime = CalendarUtil.FormatDateMD(item.getList().get(position).getDate());
                        tvStartDate.setText(mStartTime);
                        tvStartWeek.setText("周" + CalendarUtil.getWeekByFormat(item.getList().get(position).getDate()));
                        tvTime.setText("请选择结束时间");
                        tvOk.setText("请选择结束时间");
                        tvOk.setEnabled(false);
                        tvOk.setBackgroundResource(R.drawable.img_btn_bg_n);
                        llEnd.setVisibility(View.GONE);
                        tvHintText.setVisibility(View.VISIBLE);
                        return;
                    }
                    //结束
                    if (status == 0 && endGroupPosition == -1 && endChildPosition == -1) {
                        int offset = Integer.parseInt(CalendarUtil.getTwoDay(item.getList().get(position).getDate()
                                , mList.get(startGroupPosition).getList().get(startChildPosition).getDate()));
                        //判断该离开日期是否比入住时间还小，是则重新设置入住时间。
                        if (offset < 0) {
                            //刷新上一个开始日期
                            mList.get(startGroupPosition).getList().get(startChildPosition).setStatus(0);
                            mDateAdapter.notifyItemChanged(startGroupPosition);
                            //设置新的入开始日期
                            item.getList().get(position).setStatus(1);
                            startGroupPosition = helper.getAdapterPosition();
                            startChildPosition = position;
                            String mStartTime = CalendarUtil.FormatDateMD(item.getList().get(position).getDate());
                            tvStartDate.setText(mStartTime);
                            tvStartWeek.setText("周" + item.getList().get(position).getDate());
                            adapter.notifyItemChanged(position);
                            tvTime.setText("请选择结束时间");
                            tvOk.setText("请选择结束时间");//?
                            tvOk.setEnabled(false);
                            tvOk.setBackgroundResource(R.drawable.img_btn_bg_n);
                            llEnd.setVisibility(View.GONE);
                            tvHintText.setVisibility(View.VISIBLE);
                            return;
                        }
                        //结束
                        item.getList().get(position).setStatus(2);
                        adapter.notifyItemChanged(position);
                        endGroupPosition = helper.getAdapterPosition();
                        endChildPosition = position;
                        getoffsetDate(mList.get(startGroupPosition).getList().get(startChildPosition).getDate(),
                                mList.get(endGroupPosition).getList().get(endChildPosition).getDate(), true);
                        return;
                    }
                    //重置开始和结束时间，设置开始时间
                    if (status == 0 && endGroupPosition != -1 && endChildPosition != -1 && startChildPosition != -1 && startGroupPosition != -1) {
                        //重置开始和结束
                        mList.get(startGroupPosition).getList().get(startChildPosition).setStatus(0);
                        mList.get(endGroupPosition).getList().get(endChildPosition).setStatus(0);
                        mDateAdapter.notifyItemChanged(startGroupPosition);
                        mDateAdapter.notifyItemChanged(endGroupPosition);
                        //重置选择间区的状态
                        getoffsetDate(mList.get(startGroupPosition).getList().get(startChildPosition).getDate(),
                                mList.get(endGroupPosition).getList().get(endChildPosition).getDate(), false);
                        //设置入开始
                        item.getList().get(position).setStatus(1);
                        adapter.notifyItemChanged(position);
                        String mStartTime = CalendarUtil.FormatDateMD(item.getList().get(position).getDate());
                        tvStartDate.setText(mStartTime);
                        tvStartWeek.setText("周" + CalendarUtil.getWeekByFormat(item.getList().get(position).getDate()));

                        startGroupPosition = helper.getAdapterPosition();
                        startChildPosition = position;
                        endGroupPosition = -1;
                        endChildPosition = -1;
                        tvTime.setText("请选择结束时间");
                        tvOk.setText("请选择结束时间");
                        tvOk.setEnabled(false);
                        tvOk.setBackgroundResource(R.drawable.img_btn_bg_n);
                        llEnd.setVisibility(View.GONE);
                        tvHintText.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            });

        }

        public void updateData() {
            notifyDataSetChanged();
        }
    }

    private class TempAdapter extends BaseQuickAdapter<DayInfo, BaseViewHolder> {
        TempAdapter(@Nullable List<DayInfo> data) {
            super(R.layout.adapter_hotel_select_date_child, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, DayInfo item) {
            helper.setText(R.id.tv_date, item.getName());
            helper.setText(R.id.tv_dateDel, item.getName());

            //默认
            if (item.getStatus() == 0) {
                if (item.isSelect()) {
                    //选中
                    helper.getView(R.id.tv_date).setVisibility(View.VISIBLE);
                    helper.getView(R.id.tv_status).setVisibility(View.GONE);
                    helper.getView(R.id.tv_dateDel).setVisibility(View.GONE);
                    ((TextView) helper.getView(R.id.tv_date)).setTextColor(activity.getResources().getColor(R.color.white));
                    (helper.getView(R.id.ll_bg)).setBackgroundColor(activity.getResources().getColor(R.color.title_bg2));
                } else {
                    //没选中状态
                    helper.getView(R.id.tv_date).setVisibility(View.VISIBLE);
                    helper.getView(R.id.tv_status).setVisibility(View.GONE);
                    helper.getView(R.id.tv_dateDel).setVisibility(View.GONE);
                    ((TextView) helper.getView(R.id.tv_date)).setTextColor(activity.getResources().getColor(R.color.black));
                    (helper.getView(R.id.ll_bg)).setBackgroundColor(activity.getResources().getColor(R.color.white));
                }

            } else if (item.getStatus() == 1) {
                //开始
                helper.getView(R.id.tv_date).setVisibility(View.VISIBLE);
                helper.setText(R.id.tv_status, "开始");
                helper.getView(R.id.tv_status).setVisibility(View.VISIBLE);
                helper.getView(R.id.tv_dateDel).setVisibility(View.GONE);
                ((TextView) helper.getView(R.id.tv_status)).setTextColor(activity.getResources().getColor(R.color.white));
                ((TextView) helper.getView(R.id.tv_date)).setTextColor(activity.getResources().getColor(R.color.white));
                (helper.getView(R.id.ll_bg)).setBackgroundColor(activity.getResources().getColor(R.color.title_bg));
            } else if (item.getStatus() == 2) {
                //结束
                helper.getView(R.id.tv_date).setVisibility(View.VISIBLE);
                helper.setText(R.id.tv_status, "结束");
                helper.getView(R.id.tv_status).setVisibility(View.VISIBLE);
                helper.getView(R.id.tv_dateDel).setVisibility(View.GONE);
                ((TextView) helper.getView(R.id.tv_status)).setTextColor(activity.getResources().getColor(R.color.white));
                ((TextView) helper.getView(R.id.tv_date)).setTextColor(activity.getResources().getColor(R.color.white));
                (helper.getView(R.id.ll_bg)).setBackgroundColor(activity.getResources().getColor(R.color.title_bg));
            }
            //设置当前日期前的样式，没选中，并状态为0情况下
            if (!item.isSelect() && item.getStatus() == 0) {
                if (!item.isEnable()) {
                    //无效
                    TextView textView = helper.getView(R.id.tv_dateDel);
                    if (TextUtils.isEmpty(textView.getText().toString().trim())) {
                        textView.setVisibility(View.GONE);
                    } else {
                        textView.setVisibility(View.VISIBLE);
                    }
                    textView.setTextColor(activity.getResources().getColor(R.color.text_enable));
                    helper.getView(R.id.tv_date).setVisibility(View.GONE);
                    helper.getView(R.id.tv_status).setVisibility(View.GONE);

                } else {
                    helper.getView(R.id.tv_date).setVisibility(View.VISIBLE);
                    helper.getView(R.id.tv_status).setVisibility(View.GONE);
                    helper.getView(R.id.tv_dateDel).setVisibility(View.GONE);
                    TextView textView = helper.getView(R.id.tv_date);
                    textView.setTextColor(activity.getResources().getColor(R.color.black));
                }
            }
        }
    }

    private class ShareDismissListener implements OnDismissListener {
        @Override
        public void onDismiss() {
            backgroundAlpha(activity, 1f);
        }
    }
}
