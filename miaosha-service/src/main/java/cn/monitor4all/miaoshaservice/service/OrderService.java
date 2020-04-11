package cn.monitor4all.miaoshaservice.service;

public interface OrderService {

    /**
     * 创建订单
     * @param sid
     *  库存ID
     * @return
     *  订单ID
     */
    public int createWrongOrder(int sid);


    /**
     * 创建订单 乐观锁（有重试机制）
     * @param sid
     * @return
     * @throws Exception
     */
    public int createOptimisticOrder(int sid);

    /**
     * 创建订单 乐观锁（无重试机制）
     * @param sid
     * @return
     * @throws Exception
     */
    public int createOptimisticOrder2(int sid);

    /**
     * 创建订单 悲观锁 for update
     * @param sid
     * @return
     * @throws Exception
     */
    public int createPessimisticOrder(int sid);
}
