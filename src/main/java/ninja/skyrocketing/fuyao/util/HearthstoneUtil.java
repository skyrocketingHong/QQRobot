package ninja.skyrocketing.fuyao.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import ninja.skyrocketing.fuyao.bot.pojo.game.GameHsCard;
import ninja.skyrocketing.fuyao.bot.service.game.HsCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author skyrocketing Hong
 * @date 2021-03-05 22:41:18
 */

@Component
public class HearthstoneUtil {
    private static HsCardService hsCardService;
    @Autowired
    public HearthstoneUtil(HsCardService hsCardService) {
        HearthstoneUtil.hsCardService = hsCardService;
    }

    /**
    * 将json文件中的数据插入数据库中
    * @param file json文件路径
    * */
    public static void readJsonFromFile(File file) {
        JSONArray jsonArray = JSONUtil.parseArray(FileUtil.readString(file, StandardCharsets.UTF_8));
        for (int i = 0; i < jsonArray.size(); ++i) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.get("dbfId", String.class);
            String set = jsonObject.get("set", String.class);
            String name = jsonObject.get("name", String.class);
            String rarity = jsonObject.get("rarity", String.class);
            String cardId = jsonObject.get("id", String.class);
            hsCardService.insertACard(new GameHsCard(id, set, name, rarity, "https://art.hearthstonejson.com/v1/render/latest/zhCN/256x/" + cardId + ".png"));
        }
    }
}