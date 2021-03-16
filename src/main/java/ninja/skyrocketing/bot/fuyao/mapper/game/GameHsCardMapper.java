package ninja.skyrocketing.bot.fuyao.mapper.game;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ninja.skyrocketing.bot.fuyao.pojo.game.GameHsCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GameHsCardMapper extends BaseMapper<GameHsCard> {
    @Select("select * from game_hs_card where `set` = #{set,jdbcType=VARCHAR} and rarity != 'FREE' order by rand() limit 5")
    List<GameHsCard> selectBySetOrderByRandom(String setName);
}