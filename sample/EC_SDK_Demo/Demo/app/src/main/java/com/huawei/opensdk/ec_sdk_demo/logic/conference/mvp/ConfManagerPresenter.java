package com.huawei.opensdk.ec_sdk_demo.logic.conference.mvp;

import com.huawei.ecterminalsdk.base.TsdkConfMediaType;
import com.huawei.ecterminalsdk.base.TsdkConfRole;
import com.huawei.opensdk.callmgr.CallInfo;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.demoservice.ConfBaseInfo;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.ec_sdk_demo.R;
import com.huawei.opensdk.ec_sdk_demo.common.UIConstants;
import com.huawei.opensdk.ec_sdk_demo.ui.base.MVPBasePresenter;

import java.util.ArrayList;
import java.util.List;


public class ConfManagerPresenter extends MVPBasePresenter<IConfManagerContract.IConfManagerView> implements IConfManagerContract.IConfManagerPresenter
{
    private String confID;

    private String[] broadcastNames = new String[]{CustomBroadcastConstants.CONF_STATE_UPDATE,
            CustomBroadcastConstants.REQUEST_CONF_RIGHT_RESULT,
            CustomBroadcastConstants.GET_CONF_SUBSCRIBE_RESULT,
            CustomBroadcastConstants.ADD_ATTENDEE_RESULT,
            CustomBroadcastConstants.MUTE_ATTENDEE_RESULT,
            CustomBroadcastConstants.UN_MUTE_ATTENDEE_RESULT,
            CustomBroadcastConstants.MUTE_ATTENDEE_RESULT,
            CustomBroadcastConstants.UN_MUTE_CONF_RESULT,
            CustomBroadcastConstants.MUTE_CONF_RESULT,
            CustomBroadcastConstants.UN_MUTE_ATTENDEE_RESULT,
            CustomBroadcastConstants.LOCK_CONF_RESULT,
            CustomBroadcastConstants.UN_LOCK_CONF_RESULT,
            CustomBroadcastConstants.REQUEST_CHAIRMAN_RESULT,
            CustomBroadcastConstants.RELEASE_CHAIRMAN_RESULT,
            CustomBroadcastConstants.HAND_UP_RESULT,
            CustomBroadcastConstants.CANCEL_HAND_UP_RESULT,
            CustomBroadcastConstants.SET_CONF_MODE_RESULT,
            CustomBroadcastConstants.CONF_INFO_PARAM,
            CustomBroadcastConstants.UPGRADE_CONF_RESULT,
            CustomBroadcastConstants.GET_DATA_CONF_PARAM_RESULT,
            CustomBroadcastConstants.UPDATE_HOST_INFO,
            CustomBroadcastConstants.DATA_CONF_USER_LEAVE,
            CustomBroadcastConstants.DATA_CONFERENCE_JOIN_RESULT,
            CustomBroadcastConstants.DATA_CONFERENCE_USER_JOIN,
            CustomBroadcastConstants.DATA_CONFERENCE_PRESENTER_CHANGE_IND,
            CustomBroadcastConstants.DATA_CONFERENCE_HOST_CHANGE_IND,
            //CustomBroadcastConstants.CONF_CALL_CONNECTED,
            CustomBroadcastConstants.DATA_CONFERENCE_GET_DEVICE_INFO_RESULT,
            CustomBroadcastConstants.DATA_CONFERENCE_EXTEND_DEVICE_INFO,
            CustomBroadcastConstants.DATA_CONFERENCE_CAMERA_STATUS_UPDATE,
            CustomBroadcastConstants.DATE_CONFERENCE_END_AS_SHARE,
            CustomBroadcastConstants.GET_CONF_END};

    private LocBroadcastReceiver receiver = new LocBroadcastReceiver()
    {
        @Override
        public void onReceive(String broadcastName, Object obj)
        {
            int result;
            switch (broadcastName)
            {
//                //邀请自己的VoIP号码成功
//                case CustomBroadcastConstants.CONF_CALL_CONNECTED:
//                    if (obj instanceof CallInfo)
//                    {
//                        CallInfo callInfo = (CallInfo)obj;
//                        //MeetingMgr.getInstance().setCurrentConferenceCallID(callInfo.getCallID());
//                    }
//                    break;

                //申请会控权限结果，仅在失败时提示用户
                case CustomBroadcastConstants.REQUEST_CONF_RIGHT_RESULT:
                    result = (int)obj;
                    LogUtil.i(UIConstants.DEMO_TAG, "request conf ctrl right result: " + result);
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.request_conf_ctrl_fail);
                        return;
                    }
                    break;

                case CustomBroadcastConstants.CONF_STATE_UPDATE:
                    LogUtil.i(UIConstants.DEMO_TAG, "CONF_STATE_UPDATE:----- ");
                    String conferenceID = (String) obj;
                    if (!conferenceID.equals(confID))
                    {
                        return;
                    }

                    ConfBaseInfo confBaseInfo = MeetingMgr.getInstance().getCurrentConferenceBaseInfo();
                    if (confBaseInfo.getConfState() == ConfConstant.ConfConveneStatus.DESTROYED)
                    {
                        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
                        getView().finishActivity();
                        return;
                    }

                    //更新会议类型图标
                    getView().updateConfTypeIcon(confBaseInfo);
                    getView().updateUpgradeConfBtn(confBaseInfo.getMediaType() == TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO_DATA
                            || confBaseInfo.getMediaType() == TsdkConfMediaType.TSDK_E_CONF_MEDIA_VOICE_DATA);
                     Member selfEntity = getSelf();
                    if (selfEntity != null)
                    {
                        getView().updateButtons(selfEntity);
                    }

                    List <Member> memberList = MeetingMgr.getInstance().getCurrentConferenceMemberList();
                    if (memberList.isEmpty())
                    {
                        return;
                    }
                    getView().refreshMemberList(memberList);

                    break;

                // 邀请自己结果
                case CustomBroadcastConstants.ADD_SELF_RESULT:
                    result = (int)obj;
                    LogUtil.i(UIConstants.DEMO_TAG, "add self result: " + result);
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.add_self_fail);
                        return;
                    }
                    break;

                // 邀请与会者结果
                case CustomBroadcastConstants.ADD_ATTENDEE_RESULT:
                    result = (int)obj;
                    LogUtil.i(UIConstants.DEMO_TAG, "add attendee result: " + result);
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.add_attendee_fail);
                        return;
                    }
                    break;

                // 删除与会者结果
                case CustomBroadcastConstants.DEL_ATTENDEE_RESULT:
                    result = (int)obj;
                    LogUtil.i(UIConstants.DEMO_TAG, "add attendee result: " + result);
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.del_attendee_fail);
                        return;
                    }
                    break;

                // 静音与会者结果
                case CustomBroadcastConstants.MUTE_ATTENDEE_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.mute_attendee_fail);
                        return;
                    }
                    break;

                // 取消静音与会者结果
                case CustomBroadcastConstants.UN_MUTE_ATTENDEE_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.un_mute_attendee_fail);
                        return;
                    }
                    break;

                // 静音会议结果
                case CustomBroadcastConstants.MUTE_CONF_RESULT:
                    result = (int)obj;
                    if (result != 0) {
                        getView().showCustomToast(R.string.mute_conf_fail);
                    } else {
                        getView().showCustomToast(R.string.mute_conf_success);
                    }
                    break;

                // 取消静音会议结果
                case CustomBroadcastConstants.UN_MUTE_CONF_RESULT:
                    result = (int)obj;
                    if (result != 0) {
                        getView().showCustomToast(R.string.un_mute_conf_fail);
                    } else {
                        getView().showCustomToast(R.string.un_mute_conf_success);
                    }
                    break;

                // 锁定会议结果
                case CustomBroadcastConstants.LOCK_CONF_RESULT:
                    result = (int)obj;
                    if (result != 0) {
                        getView().showCustomToast(R.string.lock_conf_fail);
                    } else {
                        //getView().showCustomToast(R.string.lock_conf_success);
                    }
                    break;

                // 取消锁定会议结果
                case CustomBroadcastConstants.UN_LOCK_CONF_RESULT:
                    result = (int)obj;
                    if (result != 0) {
                        getView().showCustomToast(R.string.un_lock_conf_fail);
                    } else {
                        //getView().showCustomToast(R.string.un_lock_conf_success);
                    }
                    break;

                //  请求主席结果
                case CustomBroadcastConstants.REQUEST_CHAIRMAN_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.request_chairman_fail);
                        return;
                    }
                    break;

                // 释放主席结果
                case CustomBroadcastConstants.RELEASE_CHAIRMAN_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.release_chairman_fail);
                        return;
                    }
                    break;

                // 举手结果
                case CustomBroadcastConstants.HAND_UP_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.handup_fail);
                        return;
                    }
                    break;

                // 取消举手结果
                case CustomBroadcastConstants.CANCEL_HAND_UP_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.cancel_handup_fail);
                        return;
                    }
                    break;

                // 会议即将结束通知
                case CustomBroadcastConstants.WILL_TIMEOUT:
                    // 暂不支持
                    break;

                // 延长会议结果
                case CustomBroadcastConstants.POSTPONE_CONF_RESULT:
                    // 暂不支持
                    break;

                // 发言人通知
                case CustomBroadcastConstants.SPEAKER_LIST_IND:
                    //待实现
                    break;

                case CustomBroadcastConstants.SET_CONF_MODE_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.set_conf_mode_fail);
                        return;
                    }
                    break;

                case CustomBroadcastConstants.UPGRADE_CONF_RESULT:
                    result = (int) obj;
                    if (result != 0) {
                        getView().showCustomToast(R.string.upgrade_conf_fail);
                        return;
                    } else {
                        getView().showCustomToast(R.string.upgrade_conf_success);
                    }
                    break;

                case CustomBroadcastConstants.GET_DATA_CONF_PARAM_RESULT:
                    result = (int)obj;
                    if (result != 0)
                    {
                        updateDataConfBtn(false);
                        //getView().showCustomToast(R.string.get_data_conf_params_fail);
                        return;
                    }
                    MeetingMgr.getInstance().joinDataConf();
                    break;

                case CustomBroadcastConstants.UPDATE_HOST_INFO:
//                    conferenceEntity.setDataConfChairman((String) obj);
                    break;

                case CustomBroadcastConstants.DATA_CONFERENCE_JOIN_RESULT:
                    result = (int) obj;
                    updateDataConfBtn(result == 0);
                    if (result != 0)
                    {
                        getView().showCustomToast(R.string.join_data_conf_fail);
                    }
                    break;

                case CustomBroadcastConstants.DATA_CONFERENCE_CAMERA_STATUS_UPDATE:
                    break;

                case CustomBroadcastConstants.DATE_CONFERENCE_END_AS_SHARE:
                    getView().showCustomToast(R.string.share_end);
                    break;

                case CustomBroadcastConstants.GET_CONF_END:
                    getView().finishActivity();
                    break;

                default:
                    break;
            }
        }
    };


    /**
     * 更新数据会议按钮显示状态
     * @param show
     */
    private void updateDataConfBtn(boolean show)
    {
        getView().updateDataConfBtn(show);
    }


    private Member getSelf()
    {
        return MeetingMgr.getInstance().getCurrentConferenceSelf();
    }


    @Override
    public void registerBroadcast()
    {
        LocBroadcast.getInstance().registerBroadcast(receiver, broadcastNames);
    }

    @Override
    public String getConfID() {
        return confID;
    }

    @Override
    public void setConfID(String confID) {
        this.confID = confID;
    }

    @Override
    public ConfBaseInfo getConfBaseInfo()
    {
        return MeetingMgr.getInstance().getCurrentConferenceBaseInfo();
    }

    @Override
    public void leaveConf()
    {
        int result = MeetingMgr.getInstance().leaveConf();
        if (result != 0) {
            getView().showCustomToast(R.string.leave_conf_fail);
            return;
        }
        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
    }

    @Override
    public void endConf()
    {
        int result = MeetingMgr.getInstance().endConf();
        if (result != 0) {
            getView().showCustomToast(R.string.end_conf_fail);
            return;
        }
        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
    }

    @Override
    public void addMember(String name, String number, String account)
    {
        Member member = new Member();
        member.setNumber(number);
        member.setDisplayName(name);
        member.setAccountId(account);
        member.setRole(TsdkConfRole.TSDK_E_CONF_ROLE_ATTENDEE);

        int result = MeetingMgr.getInstance().addAttendee(member);
        if (result != 0)
        {
            getView().showCustomToast(R.string.add_attendee_fail);
        }
    }

    @Override
    public void delMember(Member member)
    {
        int result = MeetingMgr.getInstance().removeAttendee(member);
        if (result != 0)
        {
            getView().showCustomToast(R.string.del_attendee_fail);
        }
    }

    @Override
    public void muteSelf()
    {
        Member self = getSelf();
        if (self == null)
        {
            return;
        }

        boolean isMute = !self.isMute();
        int result = MeetingMgr.getInstance().muteAttendee(self, isMute);
        if (result != 0)
        {
            if (isMute) {
                getView().showCustomToast(R.string.mute_attendee_fail);
            } else {
                getView().showCustomToast(R.string.un_mute_attendee_fail);
            }
        }
    }

    @Override
    public void muteMember(Member member, boolean isMute)
    {
        int result = MeetingMgr.getInstance().muteAttendee(member, isMute);
        if (result != 0)
        {
            if (isMute) {
                getView().showCustomToast(R.string.mute_attendee_fail);
            } else {
                getView().showCustomToast(R.string.un_mute_attendee_fail);
            }
        }
    }

    @Override
    public void muteConf(boolean isMute)
    {
        int result = MeetingMgr.getInstance().muteConf(isMute);
        if (result != 0)
        {
            if (isMute) {
                getView().showCustomToast(R.string.mute_conf_fail);
            } else {
                getView().showCustomToast(R.string.un_mute_conf_fail);
            }
        }
    }

    @Override
    public void lockConf(boolean islock) {
        int result = MeetingMgr.getInstance().lockConf(islock);
        if (result != 0)
        {
            if (islock) {
                getView().showCustomToast(R.string.lock_conf_fail);
            } else {
                getView().showCustomToast(R.string.un_lock_conf_fail);
            }
        }
    }

    @Override
    public void switchLoudSpeaker()
    {
        int type = CallMgr.getInstance().switchAudioRoute();
        getView().updateLoudSpeakerButton(type);
    }

    @Override
    public void updateConf()
    {
        //若当前已在数据会议中，则不能再升级
        //TODO

        int result = MeetingMgr.getInstance().upgradeConf();
        if (result != 0)
        {
            //getView().showCustomToast(R.string.mute_attendee_fail);
        }
    }

    @Override
    public void switchConfMode() {

    }

    @Override
    public void broadcastMember(Member member) {

    }

    @Override
    public void setPresenter(Member member) {
        int result = MeetingMgr.getInstance().setPresenter(member);
        if (result != 0)
        {
//            getView().showCustomToast(R.string.mute_attendee_fail);
        }
    }

    @Override
    public void setHost(Member member) {
        int result = MeetingMgr.getInstance().setHost(member);
        if (result != 0)
        {
//            getView().showCustomToast(R.string.mute_attendee_fail);
        }
    }

    @Override
    public void handUpSelf()
    {
        Member self = getSelf();
        if (self == null)
        {
            return;
        }

        boolean isHandUp = !self.isHandUp();
        int result = MeetingMgr.getInstance().handup(isHandUp, self);
        if (result != 0)
        {
            if (isHandUp) {
                getView().showCustomToast(R.string.handup_fail);
            } else {
                getView().showCustomToast(R.string.cancel_handup_fail);
            }
        }
    }

    @Override
    public void cancelMemberHandUp(Member member) {
        int result = MeetingMgr.getInstance().handup(false, member);
        if (result != 0)
        {
            getView().showCustomToast(R.string.cancel_handup_fail);
            return;
        }
    }

    @Override
    public void releaseChairman()
    {
        int result = MeetingMgr.getInstance().releaseChairman();
        if (result != 0) {
            getView().showCustomToast(R.string.release_chairman_fail);
            return;
        }
    }

    @Override
    public void requestChairman(String chairmanPassword)
    {
        int result = MeetingMgr.getInstance().requestChairman(chairmanPassword);
        if (result != 0) {
            getView().showCustomToast(R.string.request_chairman_fail);
            return;
        }
    }

    @Override
    public void postponeConf(int time) {
        int result = MeetingMgr.getInstance().postpone(time);
        if (result != 0)
        {
            //getView().showCustomToast(R.string.mute_attendee_fail);
            return;
        }
    }

    @Override
    public void onItemClick(int position)
    {
        List<Object> items = new ArrayList<>();
        addLabel(items, position);
        if (!items.isEmpty())
        {
            getView().showItemClickDialog(items, MeetingMgr.getInstance().getCurrentConferenceMemberList().get(position));
        }
    }

    @Override
    public void onItemDetailClick(String clickedItem, Member memberEntity)
    {
        if (LocContext.getString(R.string.permit).equals(clickedItem))
        {
            muteMember(memberEntity, false);
        }
        else if (LocContext.getString(R.string.forbid).equals(clickedItem))
        {
            muteMember(memberEntity, true);
        }
        else if (LocContext.getString(R.string.cancel_hand_up).equals(clickedItem))
        {
            cancelMemberHandUp(memberEntity);
        }
        else if (LocContext.getString(R.string.hangup).equals(clickedItem))
        {
            delMember(memberEntity);
        }
        else if (LocContext.getString(R.string.reinvite).equals(clickedItem))
        {
            addMember(memberEntity.getDisplayName(), memberEntity.getNumber(), memberEntity.getAccountId());
        }
        else if (LocContext.getString(R.string.set_presenter).equals(clickedItem))
        {
            setPresenter(memberEntity);
        }
        else if (LocContext.getString(R.string.set_host).equals(clickedItem))
        {
            setHost(memberEntity);
        }
    }

    @Override
    public boolean isConfMute()
    {
        ConfBaseInfo confBaseInfo = MeetingMgr.getInstance().getCurrentConferenceBaseInfo();
        if (confBaseInfo == null)
        {
            return false;
        }
        return confBaseInfo.isMuteAll() ;
    }

    @Override
    public boolean isConfLock()
    {
        ConfBaseInfo confBaseInfo = MeetingMgr.getInstance().getCurrentConferenceBaseInfo();
        if (confBaseInfo == null)
        {
            return false;
        }
        return confBaseInfo.isLock() ;
    }

    @Override
    public boolean isChairMan()
    {
        Member self = getSelf();
        if (self == null)
        {
            return false;
        }
        return self.getRole() == TsdkConfRole.TSDK_E_CONF_ROLE_CHAIRMAN ? true : false ;
    }

    @Override
    public boolean isHandUp()
    {
        Member self = getSelf();
        if (self == null)
        {
            return false;
        }
        return self.isHandUp();
    }

    @Override
    public boolean isInDataConf()
    {
        Member self = getSelf();
        if (self == null)
        {
            return false;
        }
        return self.isInDataConference();
    }

    @Override
    public boolean isPresenter() {
        Member self = getSelf();
        if (self == null)
        {
            return false;
        }
        return self.isPresent();
    }

    @Override
    public boolean isHost()
    {
        Member self = getSelf();
        if (self == null)
        {
            return false;
        }
        return self.isHost();
    }


    private void addLabel(List<Object> items, int position)
    {
        Member member = MeetingMgr.getInstance().getCurrentConferenceMemberList().get(position);

        //设置主讲人按钮
        if (isSupportSetPresenter(member))
        {
            items.add(LocContext.getString(R.string.set_presenter));
        }

        //设置主持人按钮
        if (isSupportSetHost(member))
        {
            items.add(LocContext.getString(R.string.set_host));
        }

        if (!isChairMan())
        {
            return;
        }
        switch (member.getStatus())
        {
            case IN_CONF:
                if (member.getRole() == TsdkConfRole.TSDK_E_CONF_ROLE_CHAIRMAN)
                {
                    break;
                }
                if (member.isMute()) {
                    items.add(LocContext.getString(R.string.permit));
                } else {
                    items.add(LocContext.getString(R.string.forbid));
                }

                if (member.isHandUp()) {
                    items.add(LocContext.getString(R.string.cancel_hand_up));
                }

                items.add(LocContext.getString(R.string.hangup));
                break;

            case LEAVED:
                items.add(LocContext.getString(R.string.reinvite));
                break;

            case CALLING:
                items.add(LocContext.getString(R.string.hangup));
                break;

            default:
                break;
        }
    }

    private boolean isSupportSetPresenter(Member member)
    {
        if (isInDataConf() && (isHost() || isPresenter()))
        {
            if ((member.isInDataConference()) && (!member.isPresent()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportSetHost(Member member)
    {
//        if (isInDataConf() && isHost())
//        {
//            if ((member.isInDataConference()) && (!member.isHost()))
//            {
//                return true;
//            }
//        }
        return false;
    }

}
