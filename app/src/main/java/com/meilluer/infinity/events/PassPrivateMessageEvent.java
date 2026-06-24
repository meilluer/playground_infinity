package com.meilluer.infinity.events;

import com.meilluer.infinity.message.Message;

public class PassPrivateMessageEvent {
    public Message message;

    public PassPrivateMessageEvent(Message message) {
        this.message = message;
    }
}
