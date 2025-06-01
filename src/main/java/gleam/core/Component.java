package gleam.core;

import gleam.communication.MessageHandler;
import gleam.communication.Protocol;
import gleam.core.event.GameEventListener;

/**
 * 组件
 * 
 * @author hdh
 *
 */
public interface Component extends GameEventListener, MessageHandler<Protocol, Protocol>, LifeCycle {

}
