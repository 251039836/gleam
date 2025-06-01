package gleam.core.service;

import gleam.core.Component;
import gleam.core.define.ServiceStatus;

/**
 * 服务<br>
 * 公共模块<br>
 * 
 * @author hdh
 *
 */
public interface Service extends Component, Comparable<Service> {

	int PRIORITY_LOWEST = 10;
	int PRIORITY_LOW = 30;
	int PRIORITY_NORMAL = 50;
	int PRIORITY_HIGH = 70;
	int PRIORITY_HIGHEST = 90;

	String getName();

	/**
	 * 启动优先级
	 * 
	 * @return
	 */
	int getPriority();

	ServiceStatus getStatus();

}
