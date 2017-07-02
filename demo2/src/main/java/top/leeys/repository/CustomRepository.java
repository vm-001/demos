package top.leeys.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import top.leeys.domain.User;
import top.leeys.dto.RankDTO;
import top.leeys.dto.UserDTO;


/**
 * 复杂的查询使用JdbcTemplate做原生SQL查询
 * 
 * @author leeys.top@gmail.com
 */
@Repository
public class CustomRepository {
    @Autowired JdbcTemplate jdbcTemplate;
    
    /**
     * 根据uuid获取用户信息包括游戏数据(排名、总成绩、游戏局数)
     */
    public UserDTO getUserInfo(String uuid) {
        final UserDTO userDTO = new UserDTO();
        String sql = "SELECT * FROM (SELECT rank.*, (@row := @row+1) row_no FROM (SELECT(@row := 0)) b, (SELECT u.uuid, u.username, u.avatar_url, u.description, u.uid, IFNULL(SUM(r.score), 0) total_score, COUNT(r.id) play_num FROM tb_user u LEFT JOIN tb_record r ON u.uuid = r.user_uuid GROUP BY r.user_uuid ORDER BY total_score DESC, play_num ASC) rank ) ranking " + 
                " where ranking.uuid = ?";
        jdbcTemplate.query(sql, new Object[] {uuid}, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                User user = new User();
                user.setUuid(rs.getString("uuid"));
                user.setAvatarUrl(rs.getString("avatar_url"));
                user.setUsername(rs.getString("username"));
                user.setDescription(rs.getString("description"));
                user.setUid(rs.getString("uid"));
                userDTO.setUser(user);
                userDTO.setTotalScore(rs.getInt("total_score"));
                userDTO.setPlayNum(rs.getInt("play_num"));
                userDTO.setRank(rs.getInt("row_no"));
            }
        });
        return userDTO;
    }
    
    public List<RankDTO> getRankInfo() {
        final List<RankDTO> rankDTOList = new ArrayList<>();
//        String sql = "SELECT u.uuid, u.avatar_url, u.username, u.description, u.uid, sum(r.score) total_score FROM tb_record r, tb_user u WHERE r.user_uuid = u.uuid GROUP BY r.user_uuid ORDER BY total_score DESC LIMIT 10";
        String sql = "SELECT u.uuid, u.avatar_url, u.username, u.description, u.uid, SUM(r.score) total_score, COUNT(r.id) play_num FROM tb_record r, tb_user u WHERE r.user_uuid = u.uuid GROUP BY r.user_uuid ORDER BY total_score DESC, play_num ASC LIMIT 10";
        jdbcTemplate.query(sql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                RankDTO rankDTO = new RankDTO();
                User user = new User();
                user.setUuid(rs.getString("uuid"));
                user.setAvatarUrl(rs.getString("avatar_url"));
                user.setUsername(rs.getString("username"));
                user.setDescription(rs.getString("description"));
                user.setUid(rs.getString("uid"));
                rankDTO.setUser(user);
                rankDTO.setTotalScore(rs.getInt("total_score"));
                rankDTOList.add(rankDTO);
            }   
        });
        return rankDTOList;
    }
    
    
}
