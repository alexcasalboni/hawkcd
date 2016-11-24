package io.hawkcd.core;

import io.hawkcd.model.ServiceResult;
import io.hawkcd.model.User;
import io.hawkcd.model.dto.WsContractDto;

/**
 * Created by Rado @radoslavMinchev, rminchev@rnd-solutions.net on 24.11.16.
 */
public class MessageConverter {

    public static WsContractDto convert(String className, String packageName, String methodName, ServiceResult sReslult) {

        WsContractDto contract = new WsContractDto(className
                , packageName
                , methodName
                , sReslult.getEntity()
                , sReslult.getNotificationType()
                , sReslult.getMessage());

        return null;
    }

    public static WsContractDto convert(Message message) {

        return new WsContractDto(message.getServiceCalled()
                , message.getPackageName()
                , message.getMethodCalled()
                , message.getResultObject()
                , message.getResultNotificationType()
                , message.getResultMessage());
    }

    public static Message convert(User user, String className,String packageName, String methodName, ServiceResult sResult) {

        return new Message(
                className,
                packageName,
                methodName,
                sResult.getEntity(),
                sResult.getNotificationType(),
                sResult.getMessage(),
                user
        );
    }
}
