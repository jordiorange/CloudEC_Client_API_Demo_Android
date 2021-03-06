package com.huawei.opensdk.ec_sdk_demo.ui.conference;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.huawei.ecterminalsdk.base.TsdkConfMediaType;
import com.huawei.opensdk.callmgr.CallConstant;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.demoservice.ConfBaseInfo;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.ec_sdk_demo.R;
import com.huawei.opensdk.ec_sdk_demo.adapter.ConfManagerAdapter;
import com.huawei.opensdk.ec_sdk_demo.common.UIConstants;
import com.huawei.opensdk.ec_sdk_demo.logic.conference.mvp.ConfManagerPresenter;
import com.huawei.opensdk.ec_sdk_demo.logic.conference.mvp.IConfManagerContract;
import com.huawei.opensdk.ec_sdk_demo.ui.IntentConstant;
import com.huawei.opensdk.ec_sdk_demo.ui.base.MVPBaseActivity;
import com.huawei.opensdk.ec_sdk_demo.util.ActivityUtil;
import com.huawei.opensdk.ec_sdk_demo.util.CommonUtil;
import com.huawei.opensdk.ec_sdk_demo.widget.ConfirmDialog;
import com.huawei.opensdk.ec_sdk_demo.widget.EditDialog;
import com.huawei.opensdk.ec_sdk_demo.widget.SimpleListDialog;
import com.huawei.opensdk.ec_sdk_demo.widget.ThreeInputDialog;
import com.huawei.opensdk.ec_sdk_demo.widget.TripleDialog;

import java.util.List;


public class ConfManagerActivity extends MVPBaseActivity<IConfManagerContract.IConfManagerView, ConfManagerPresenter> implements IConfManagerContract.IConfManagerView, View.OnClickListener
{

    private ConfManagerPresenter mPresenter;
    private ConfManagerAdapter adapter;

    private LinearLayout confButtonGroup;
    private ListView confListView;
    private ImageView existConfIV;
    private ImageView muteSelfIV;
    private ImageView loudSpeakerIV;
    private ImageView updateConfIV;
    private ImageView btnMoreIV;
    private TextView titleTV;
    private ImageView btnShareIV;
    private ImageView btnVideoIV;
    PopupWindow mPopupWindow;

    private String confID;

    @Override
    protected IConfManagerContract.IConfManagerView createView()
    {
        return this;
    }

    @Override
    protected ConfManagerPresenter createPresenter()
    {
        mPresenter = new ConfManagerPresenter();
        return mPresenter;
    }

    @Override
    public void initializeComposition()
    {
        setContentView(R.layout.conf_manager_layout);
        confButtonGroup = (LinearLayout) findViewById(R.id.media_btn_group);
        confListView = (ListView) findViewById(R.id.member_list);
        existConfIV = (ImageView) findViewById(R.id.conf_hangup_iv);
        muteSelfIV = (ImageView) findViewById(R.id.conf_mute_iv);
        loudSpeakerIV = (ImageView) findViewById(R.id.conf_loud_speaker_iv);
        updateConfIV = (ImageView) findViewById(R.id.conf_update_iv);
        btnMoreIV = (ImageView) findViewById(R.id.conf_btn_more);
        titleTV = (TextView) findViewById(R.id.title_text);
        btnVideoIV = (ImageView) findViewById(R.id.video_view);
        btnShareIV = (ImageView) findViewById(R.id.share_view);

        updateConfIV.setVisibility(View.GONE);

        existConfIV.setOnClickListener(this);
        muteSelfIV.setOnClickListener(this);
        loudSpeakerIV.setOnClickListener(this);
        updateConfIV.setOnClickListener(this);
        btnMoreIV.setOnClickListener(this);
        btnVideoIV.setOnClickListener(this);
        btnShareIV.setOnClickListener(this);

        confListView.setAdapter(adapter);

        confListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                mPresenter.onItemClick(position);
            }
        });
    }

    @Override
    public void initializeData()
    {
        Intent intent = getIntent();
        confID = intent.getStringExtra(UIConstants.CONF_ID);

        mPresenter.setConfID(confID);
        mPresenter.registerBroadcast();
        adapter = new ConfManagerAdapter(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        updateLoudSpeakerButton(CallMgr.getInstance().getCurrentAudioRoute());
    }

    @Override
    public void showLoading()
    {

    }

    @Override
    public void dismissLoading()
    {

    }

    @Override
    public void showCustomToast(final int resID)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                showToast(resID);
            }
        });
    }

    @Override
    protected void onBack()
    {
        mPresenter.leaveConf();
        super.onBack();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.conf_hangup_iv:
                if (!mPresenter.isChairMan())
                {
                    showLeaveConfDialog();
                    break;
                }
                showEndConfDialog();
                break;
            case R.id.conf_mute_iv:
                mPresenter.muteSelf();
                break;
            case R.id.conf_loud_speaker_iv:
                mPresenter.switchLoudSpeaker();
                break;
            case R.id.conf_update_iv:
                mPresenter.updateConf();
                break;
            case R.id.conf_btn_more:
                showMoreButton();
                break;

            case R.id.video_view:
                Intent intent = new Intent(IntentConstant.VIDEO_CONF_ACTIVITY_ACTION);
                intent.putExtra(UIConstants.CONF_ID, mPresenter.getConfID());
                ActivityUtil.startActivity(this, intent);
                break;

            case R.id.share_view:
                Intent intent1 = new Intent(IntentConstant.Conf_DATA_ACTIVITY_ACTION);
                intent1.putExtra(UIConstants.CONF_ID, mPresenter.getConfID());
                ActivityUtil.startActivity(this, intent1);
                break;
            default:
                break;
        }
    }

    private void showLeaveConfDialog()
    {
        ConfirmDialog dialog = new ConfirmDialog(this, R.string.leave_conf);
        dialog.setRightButtonListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPresenter.leaveConf();
                finish();
            }
        });
        dialog.show();
    }

    private void showMoreButton()
    {
//        int solution = LoginCenter.getInstance().getSolution();
        int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
        View popupView = getLayoutInflater().inflate(R.layout.popup_conf_btn_list, null);

        LinearLayout addAttendeeLayout = (LinearLayout) popupView.findViewById(R.id.add_attendee_ll);
        LinearLayout handUpLayout = (LinearLayout) popupView.findViewById(R.id.hand_up_ll);
        LinearLayout muteAllLayout = (LinearLayout) popupView.findViewById(R.id.mute_all_ll);
        LinearLayout cancelMuteAllLayout = (LinearLayout) popupView.findViewById(R.id.cancel_mute_all_ll);
        LinearLayout lockLayout = (LinearLayout) popupView.findViewById(R.id.lock_conf_ll);
        LinearLayout unlockLayout = (LinearLayout) popupView.findViewById(R.id.un_lock_conf_ll);
        LinearLayout requestChairManLayout = (LinearLayout) popupView.findViewById(R.id.request_chairman_ll);
        LinearLayout releaseChairManLayout = (LinearLayout) popupView.findViewById(R.id.release_chairman_ll);
        ImageView handUpIV = (ImageView) popupView.findViewById(R.id.hand_up_iv);
        TextView handUpTV = (TextView) popupView.findViewById(R.id.hand_up_tv);
        if (mPresenter.isChairMan()) {

            if (mPresenter.isConfMute()) {
                cancelMuteAllLayout.setVisibility(View.VISIBLE);
                muteAllLayout.setVisibility(View.GONE);
            } else {
                cancelMuteAllLayout.setVisibility(View.GONE);
                muteAllLayout.setVisibility(View.VISIBLE);
            }

//            if (solution == LoginCenter.getInstance().CLOUD_EC) {

                requestChairManLayout.setVisibility(View.GONE);
                releaseChairManLayout.setVisibility(View.VISIBLE);

                if (mPresenter.isConfLock()) {
                    unlockLayout.setVisibility(View.VISIBLE);
                    lockLayout.setVisibility(View.GONE);
                } else {
                    unlockLayout.setVisibility(View.GONE);
                    lockLayout.setVisibility(View.VISIBLE);
                }

                handUpLayout.setVisibility(View.GONE);
//            } else {
//                // Cloud PBX 下不支持相关功能
//                requestChairManLayout.setVisibility(View.GONE);
//                releaseChairManLayout.setVisibility(View.GONE);
//                unlockLayout.setVisibility(View.GONE);
//                lockLayout.setVisibility(View.GONE);
//                handUpLayout.setVisibility(View.GONE);
//            }
        } else {
            cancelMuteAllLayout.setVisibility(View.GONE);
            muteAllLayout.setVisibility(View.GONE);
            unlockLayout.setVisibility(View.GONE);
            lockLayout.setVisibility(View.GONE);

//            if (solution == LoginCenter.getInstance().CLOUD_EC) {
                requestChairManLayout.setVisibility(View.VISIBLE);
                releaseChairManLayout.setVisibility(View.GONE);

                if (mPresenter.isHandUp()) {
                    handUpIV.setActivated(false);
                    handUpTV.setText(R.string.conf_cancel_hand_up);
                } else {
                    handUpIV.setActivated(true);
                    handUpTV.setText(R.string.conf_hand_up);
                }
//            } else {
//                // Cloud PBX 下不支持相关功能
//                requestChairManLayout.setVisibility(View.GONE);
//                releaseChairManLayout.setVisibility(View.GONE);
//                handUpLayout.setVisibility(View.GONE);
//            }
        }

        addAttendeeLayout.setOnClickListener(moreButtonListener);
        handUpLayout.setOnClickListener(moreButtonListener);
        muteAllLayout.setOnClickListener(moreButtonListener);
        cancelMuteAllLayout.setOnClickListener(moreButtonListener);
        lockLayout.setOnClickListener(moreButtonListener);
        unlockLayout.setOnClickListener(moreButtonListener);
        requestChairManLayout.setOnClickListener(moreButtonListener);
        releaseChairManLayout.setOnClickListener(moreButtonListener);

        mPopupWindow = generatePopupWindow(popupView, wrap, wrap);
        mPopupWindow.showAtLocation(findViewById(R.id.conf_manager_ll), Gravity.RIGHT | Gravity.BOTTOM, 0, confButtonGroup.getHeight());
    }

    private View.OnClickListener moreButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mPopupWindow != null && mPopupWindow.isShowing())
            {
                mPopupWindow.dismiss();
            }
            switch (v.getId())
            {
                case R.id.add_attendee_ll:
                    showAddMemberDialog();
                    break;
                case R.id.hand_up_ll:
                    mPresenter.handUpSelf();
                    break;
                case R.id.mute_all_ll:
                    mPresenter.muteConf(true);
                    break;
                case R.id.cancel_mute_all_ll:
                    mPresenter.muteConf(false);
                    break;
                case R.id.lock_conf_ll:
                    mPresenter.lockConf(true);
                    break;
                case R.id.un_lock_conf_ll:
                    mPresenter.lockConf(false);
                    break;
                case R.id.request_chairman_ll:
                    showRequestChairmanDialog();
                    break;
                case R.id.release_chairman_ll:
                    mPresenter.releaseChairman();
                    break;
                default:
                    break;
            }
        }
    };

    private void showRequestChairmanDialog()
    {
        final EditDialog dialog = new EditDialog(this, R.string.input_chairman_password);
        dialog.setRightButtonListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CommonUtil.hideSoftInput(ConfManagerActivity.this);
                if (TextUtils.isEmpty(dialog.getText()))
                {
                    showToast(R.string.invalid_password);
                    return;
                }
                mPresenter.requestChairman(dialog.getText());
            }
        });
        dialog.show();
    }

    private synchronized PopupWindow generatePopupWindow(View view, int width, int height)
    {
        final PopupWindow popupWindow = new PopupWindow(view, width, height);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_MENU && popupWindow.isShowing())
                {
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

        return popupWindow;
    }


    public void updateMuteButton(final boolean mute)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                muteSelfIV.setActivated(mute);
            }
        });
    }

    private void showAddMemberDialog()
    {
        final ThreeInputDialog editDialog = new ThreeInputDialog(this);
        editDialog.setRightButtonListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPresenter.addMember(editDialog.getInput2(), editDialog.getInput1(), editDialog.getInput3());
            }
        });

        editDialog.setHint1(R.string.input_number);
        editDialog.setHint2(R.string.input_name);
        editDialog.setHint3(R.string.input_account);
        editDialog.show();
    }

    @Override
    public void refreshMemberList(final List<Member> list)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                adapter.setData(list);
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void updateConfTypeIcon(final ConfBaseInfo confEntity)
    {
//        final int solution = LoginCenter.getInstance().getSolution();

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if ((TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO == confEntity.getMediaType())
                        || (TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO_DATA == confEntity.getMediaType()))
                {
                    btnVideoIV.setVisibility(View.VISIBLE);
                }


                if ((TsdkConfMediaType.TSDK_E_CONF_MEDIA_VOICE_DATA == confEntity.getMediaType())
                        || (TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO_DATA == confEntity.getMediaType()))
                {
                    if (mPresenter.isInDataConf()) {
                        btnShareIV.setVisibility(View.VISIBLE);
                    }
                }

                if (confEntity != null && confEntity.getSubject() != null)
                {
                    titleTV.setVisibility(View.VISIBLE);
                    titleTV.setText(confEntity.getSubject());
                }
            }
        });
    }

    @Override
    public void updateDataConfBtn(final boolean show)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                btnShareIV.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void updateVideoBtn(final boolean show)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                btnVideoIV.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void updateUpgradeConfBtn(final boolean isDataConf)
    {
        if (mPresenter.isChairMan()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConfIV.setVisibility(isDataConf ? View.GONE : View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void updateButtons(Member conferenceMemberEntity)
    {
        updateMuteButton(conferenceMemberEntity.isMute());
    }

    @Override
    public void updateLoudSpeakerButton(int type)
    {
        if (type == CallConstant.TYPE_LOUD_SPEAKER)
        {
            loudSpeakerIV.setActivated(true);
        }
        else
        {
            loudSpeakerIV.setActivated(false);
        }
    }

    @Override
    public void updateTitle(final String title)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                titleTV.setText(title);
                titleTV.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showItemClickDialog(final List<Object> items, final Member member)
    {
        final SimpleListDialog dialog = new SimpleListDialog(this, items);
        dialog.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                dialog.dismiss();
                mPresenter.onItemDetailClick((String) items.get(position), member);
            }
        });
        dialog.show();
    }

    @Override
    public void finishActivity()
    {
        finish();
    }

    public void showEndConfDialog()
    {
        TripleDialog dialog = new TripleDialog(this);
        dialog.setRightButtonListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPresenter.leaveConf();
                finish();
            }
        });
        dialog.setLeftButtonListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPresenter.endConf();
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed()
    {
        mPresenter.leaveConf();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy()
    {
        mPresenter.leaveConf();
        super.onDestroy();
    }
}
