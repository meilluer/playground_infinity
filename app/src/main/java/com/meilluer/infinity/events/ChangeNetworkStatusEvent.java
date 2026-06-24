package com.meilluer.infinity.events;

public class ChangeNetworkStatusEvent {
    public int connectedNetwork;

    public ChangeNetworkStatusEvent(int connectedNetwork) {
        this.connectedNetwork = connectedNetwork;
    }
}
