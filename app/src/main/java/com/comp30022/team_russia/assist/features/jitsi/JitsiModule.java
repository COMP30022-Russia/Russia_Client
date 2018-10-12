package com.comp30022.team_russia.assist.features.jitsi;

import com.comp30022.team_russia.assist.features.jitsi.models.VoiceEndedPushNoti;
import com.comp30022.team_russia.assist.features.jitsi.models.VoicePendingAcceptPushNoti;
import com.comp30022.team_russia.assist.features.jitsi.models.VoiceStartedPushNoti;
import com.comp30022.team_russia.assist.features.jitsi.models.VoiceStateChangedPushNoti;
import com.comp30022.team_russia.assist.features.jitsi.services.JitsiMeetHolder;
import com.comp30022.team_russia.assist.features.jitsi.services.JitsiMeetHolderImpl;
import com.comp30022.team_russia.assist.features.jitsi.services.VoiceCoordinator;
import com.comp30022.team_russia.assist.features.jitsi.services.VoiceCoordinatorImpl;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;

import dagger.Binds;
import dagger.Module;

import javax.inject.Singleton;

/**
 * Dagger module for Jitsi-related features (voice and video call).
 *
 * <p>Jitsi Meet is the open-source SDK we use for voice and video call.
 */
@Module
public abstract class JitsiModule {

    @Singleton
    @Binds
    public abstract JitsiMeetHolder bindJitsiMeetHolder(JitsiMeetHolderImpl jitsiMeetHolder);

    @Singleton
    @Binds
    public abstract VoiceCoordinator bindVoiceCoordinator(VoiceCoordinatorImpl voiceCoordinator);

    public static void configureGlobalTopics(PubSubHub pubSubHub) {
        pubSubHub.configureTopic(PubSubTopics.JITSI_IDLE, Void.class,
            PayloadToObjectConverter.createForVoidPayload());


        pubSubHub.configureTopic(PubSubTopics.JITSI_JOINED, Void.class,
            PayloadToObjectConverter.createForVoidPayload());

        pubSubHub.configureTopic(PubSubTopics.JITSI_LEFT, Void.class,
            PayloadToObjectConverter.createForVoidPayload());

        pubSubHub.configureTopic(PubSubTopics.JITSI_FAILED, Void.class,
            PayloadToObjectConverter.createForVoidPayload());

        pubSubHub.configureTopic(PubSubTopics.JITSI_STOPPED, Void.class,
            PayloadToObjectConverter.createForVoidPayload());

        pubSubHub.configureTopic(PubSubTopics.NAV_CALL_PENDING_ME, VoicePendingAcceptPushNoti.class,
            PayloadToObjectConverter.createGsonForType(VoicePendingAcceptPushNoti.class));

        pubSubHub.configureTopic(PubSubTopics.NAV_CALL_PENDING_OTHER,
            VoicePendingAcceptPushNoti.class,
            PayloadToObjectConverter.createGsonForType(VoicePendingAcceptPushNoti.class));

        pubSubHub.configureTopic(PubSubTopics.NAV_CALL_STARTED, VoiceStartedPushNoti.class,
            PayloadToObjectConverter.createGsonForType(VoiceStartedPushNoti.class));

        pubSubHub.configureTopic(PubSubTopics.NAV_CALL_TERMINATED, VoiceEndedPushNoti.class,
            PayloadToObjectConverter.createGsonForType(VoiceEndedPushNoti.class));

        pubSubHub.configureTopic(PubSubTopics.NAV_CALL_STATE_CHANGED,
            VoiceStateChangedPushNoti.class,
            PayloadToObjectConverter.createGsonForType(VoiceStateChangedPushNoti.class));

        pubSubHub.configureTopic(PubSubTopics.NAV_COORDINATOR_ERROR_MESSAGE, String.class,
            new PayloadToObjectConverter<String>() {
                @Override
                public String fromString(String payloadStr) {
                    return payloadStr;
                }

                @Override
                public String toString(String payload) {
                    return payload;
                }
            });

        pubSubHub.configureTopic(PubSubTopics.JITSI_PLEASE_START, JitsiStartArgs.class,
            PayloadToObjectConverter.createGsonForType(JitsiStartArgs.class));

        pubSubHub.configureTopic(PubSubTopics.JITSI_PLEASE_STOP, Void.class,
            PayloadToObjectConverter.createForVoidPayload());
    }
}
