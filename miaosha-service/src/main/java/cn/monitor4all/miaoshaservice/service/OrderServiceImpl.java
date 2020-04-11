package cn.monitor4all.miaoshaservice.service;

import cn.monitor4all.miaoshadao.dao.Stock;
import cn.monitor4all.miaoshadao.dao.StockOrder;
import cn.monitor4all.miaoshadao.mapper.StockOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private StockService stockService;

    @Autowired
    private StockOrderMapper orderMapper;

    @Override
    public int createWrongOrder(int sid) {
        //校验库存
        Stock stock = checkStock(sid);
        //扣库存
        saleStock(stock);
        //创建订单
        int id = createOrder(stock);
        return id;
    }

    //乐观锁有重试机制
    @Override
    public int createOptimisticOrder(int sid) {
        LOGGER.info("===========createOptimisticOrder1");
        //校验库存
        Stock stock = checkStock(sid);
        //乐观锁更新库存
        int count = saleStockOptimistic(stock);
        //如果更新失败尝试重新更新
        if (count<=0){
            Stock checkStock = checkStock(sid);
            //如果库存大于0才重试
            if (checkStock.getCount()>0){
                createOptimisticOrder(sid);
            }
        }else{
            //创建订单
            createOrder(stock);
        }

        return stock.getCount();
    }

    //乐观锁无重试机制
    @Override
    public int createOptimisticOrder2(int sid) {
        LOGGER.info("===========createOptimisticOrder2");
        //校验库存
        Stock stock = checkStock(sid);
        //乐观锁更新库存
        saleStockOptimistic2(stock);
            //创建订单
        createOrder(stock);
        return stock.getCount();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int createPessimisticOrder(int sid){
        //校验库存(悲观锁for update)
        Stock stock = checkStockForUpdate(sid);
        if (stock.getCount()>0){
            //更新库存
            saleStock(stock);
            //创建订单
            createOrder(stock);
        }
        //剩余库存
        return stock.getCount();
    }

    /**
     * 检查库存
     * @param sid
     * @return
     */
    private Stock checkStock(int sid) {
        Stock stock = stockService.getStockById(sid);
        if (stock.getCount()<=0) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    /**
     * 检查库存 ForUpdate
     * @param sid
     * @return
     */
    private Stock checkStockForUpdate(int sid) {
        Stock stock = stockService.getStockByIdForUpdate(sid);
        if (stock.getCount()<=0) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    /**
     * 更新库存
     * @param stock
     */
    private void saleStock(Stock stock) {
        stock.setSale(stock.getSale() + 1);
        stock.setCount(stock.getCount()-1);
        stockService.updateStockById(stock);
    }

    /**
     * 创建订单
     * @param stock
     * @return
     */
    private int createOrder(Stock stock) {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        int id = orderMapper.insertSelective(order);
        return id;
    }

    /**
     * 更新库存 乐观锁
     * @param stock
     */
    private int saleStockOptimistic(Stock stock) {
        LOGGER.info("查询数据库，尝试更新库存");
        int count = stockService.updateStockByOptimistic(stock);
        //更新失败触发重试机制
        return count;
    }

    /**
     * 更新库存 乐观锁
     * @param stock
     */
    private void saleStockOptimistic2(Stock stock) {
        LOGGER.info("查询数据库，尝试更新库存");
        int count = stockService.updateStockByOptimistic(stock);
        if (count <=0){
            throw new RuntimeException("===更新失败");
        }
        //更新失败触发重试机制
    }

//    public static void main(String[] args) {
//        String str = "";
//        for (int i=1;i<=100;i++){
//            if (i%3==0 && i%5==0){
//                str=str+"@";
//            }else if (i%3==0){
//                str = str+"*";
//            }else if (i%5==0){
//                str = str+"#";
//            }
//            else{
//                str = str + i;
//            }
//        }
//        System.out.println(str);
//    }
}
