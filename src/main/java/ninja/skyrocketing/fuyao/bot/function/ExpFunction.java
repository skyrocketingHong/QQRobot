package ninja.skyrocketing.fuyao.bot.function;

import cn.hutool.core.date.DateUtil;
import lombok.NoArgsConstructor;
import net.mamoe.mirai.message.data.Message;
import ninja.skyrocketing.fuyao.bot.pojo.group.GroupExp;
import ninja.skyrocketing.fuyao.bot.pojo.group.GroupExpRankName;
import ninja.skyrocketing.fuyao.bot.pojo.group.GroupMessage;
import ninja.skyrocketing.fuyao.bot.pojo.group.GroupUser;
import ninja.skyrocketing.fuyao.bot.service.group.GroupExpRankNameService;
import ninja.skyrocketing.fuyao.bot.service.group.GroupExpService;
import ninja.skyrocketing.fuyao.util.RandomUtil;
import ninja.skyrocketing.fuyao.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author skyrocketing Hong
 * @date 2020-11-28 15:56:35
 */

@Component
@NoArgsConstructor
public class ExpFunction {
    private static GroupExpService groupExpService;
    private static GroupExpRankNameService groupExpRankNameService;

    @Autowired
    private ExpFunction(GroupExpService groupExpService, GroupExpRankNameService groupExpRankNameService) {
        ExpFunction.groupExpService = groupExpService;
        ExpFunction.groupExpRankNameService = groupExpRankNameService;
    }

    //签到
    public static Message signIn(GroupMessage groupMessage) {
        //创建群号和QQ号的实例
        GroupUser groupUser = groupMessage.getGroupUser();
        //从数据库中获取当前群中的用户的数据
        GroupExp groupExp = groupExpService.getExpByGroupUser(groupUser);
        //获取当前日期
        Date nowDate = DateUtil.date();
        //随机生成一个经验值
        int exp = RandomUtil.randomNum(10) + 10;
        //用户不存在时，直接插入一条新的数据
        if (groupExp == null) {
            groupExp = new GroupExp(groupUser, exp);
            int status = groupExpService.insertExp(groupExp);
            //数据库问题，插入失败
            if (status == 0) {
                groupMessage.getMessageChainBuilder().add("❌ 签到失败" + "\n" + "请联系开发者查看数据库是否出现问题");
                return groupMessage.getMessageChainBuilderAsMessageChain();
            }
            //签到成功
            groupMessage.getMessageChainBuilder().add("✔ 首次签到成功" + "\n" +
                    "获取 " + exp + " 经验值" + "\n" +
                    "下次签到时间 " + TimeUtil.dateFormatter(new Date(nowDate.getTime() + 28800000))
            );
            return groupMessage.getMessageChainBuilderAsMessageChain();
        }
        //用户存在时，再判断是否可以签到
        else {
            //获取上次签到时间
            Date lastSignInDate = groupExp.getSignInDate();
            //如果上次签到时间与当前时间间隔小于8小时，则直接返回消息
            if (nowDate.getTime() - lastSignInDate.getTime() <= 28800000) {
                groupMessage.getMessageChainBuilder().add("❌ 签到失败" + "\n" +
                        "下次签到时间 " + TimeUtil.dateFormatter(new Date(lastSignInDate.getTime() + 28800000))
                );
                return groupMessage.getMessageChainBuilderAsMessageChain();
            }
            //所有条件满足后，直接签到
            else {
                //将对象的值改为下一次需要写回数据库的值
                groupExp.nextExp(exp, nowDate);
                int status = groupExpService.updateExp(groupExp);
                //数据库问题，插入失败
                if (status == 0) {
                    groupMessage.getMessageChainBuilder().add("❌ 签到失败" + "\n" + "请联系开发者查看数据库是否出现问题");
                    return groupMessage.getMessageChainBuilderAsMessageChain();
                }
            }
        }
        //签到成功
        groupMessage.getMessageChainBuilder().add("✔ 签到成功" + "\n" +
                "获取 " + exp + " 经验值" + "\n" +
                "下次签到时间 " + TimeUtil.dateFormatter(new Date(groupExp.getSignInDate().getTime() + 28800000))
        );
        return groupMessage.getMessageChainBuilderAsMessageChain();
    }

    //EXP查询
    public static Message expQuery(GroupMessage groupMessage) {
        //创建群号和QQ号的实例
        GroupUser groupUser = groupMessage.getGroupUser();
        //从数据库中获取当前群中的用户的数据
        GroupExp groupExp = groupExpService.getExpByGroupUser(groupUser);
        if (groupExp != null) {
            long exp = groupExp.getExp();
            groupMessage.getMessageChainBuilder().add("🔰 当前等级 " + expRank(groupMessage.getGroupUser().getGroupId(), exp) + "\n" +
                    "🍔 经验值为 " + exp
            );
        } else {
            groupMessage.getMessageChainBuilder().add("❌ 当前群未签到");
        }
        return groupMessage.getMessageChainBuilderAsMessageChain();
    }

    //EXP对应等级
    public static String expRank(long id, long exp) {
        GroupExpRankName groupExpRankName = groupExpRankNameService.getGroupExpRankNameById(id);
        if (groupExpRankName == null) {
            groupExpRankName = groupExpRankNameService.getGroupExpRankNameById(0L);
        }
        exp -= groupExpRankName.getExpOffset();
        if (exp <= 50) {
            return groupExpRankName.getRank1();
        } else if (exp <= 150) {
            return groupExpRankName.getRank2();
        } else if (exp <= 250) {
            return groupExpRankName.getRank3();
        } else if (exp <= 400) {
            return groupExpRankName.getRank4();
        } else if (exp <= 700) {
            return groupExpRankName.getRank5();
        } else if (exp <= 1000) {
            return groupExpRankName.getRank6();
        } else {
            return groupExpRankName.getRank7();
        }
    }

    //清除EXP数据
    public static void cleanExpData(Long groupId, Long userId) {
        if (userId == 0L) {
            groupExpService.deleteExpByGroupId(groupId);
            return;
        }
        if (groupId == 0L) {

        }
        groupExpService.deleteExp(new GroupUser(groupId, userId));
    }
}
