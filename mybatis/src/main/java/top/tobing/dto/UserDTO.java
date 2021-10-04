package top.tobing.dto;

/**
 * @author tobing
 * @date 2021/10/3 23:33
 * @description
 */
public class UserDTO {

    private Integer id;
    private String username;
    private String gender;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
