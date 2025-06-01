# gleam
一个java实现的游戏服务端框架.  
基于actor模型+ecs的大幅度修改简化实现.  
此处的actor并未对外隔离,外界依然可以直接操作内部数据,需自行进行并发处理,以方便游戏中大部分单服玩法开发.  
跨服玩法可使用entityRef类似actorRef的使用方式进行操作,此处实现了一套较为简陋的定位转发逻辑.  
ecs在此处默认只使用entity+component,component除了对应功能的数据外还实现事件监听+消息处理.  
若某些玩法有批量操作数据的操作可自行将对应数据统一放到service管理,再在component引用数据.  


