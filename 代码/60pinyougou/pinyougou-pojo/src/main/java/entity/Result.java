package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * result 用于 返回给页面 表示是否成功 成功与否的错误信息
 *
 * @author 三国的包子
 * @version 1.0
 * @package entity *
 * @since 1.0
 */
public class Result  implements Serializable{
    private boolean success;

    private String message;

    //错误信息
    private List<Error> errorsList= new ArrayList<>();

    public List<Error> getErrorsList() {
        return errorsList;
    }

    public void setErrorsList(List<Error> errorsList) {
        this.errorsList = errorsList;
    }

    public Result(boolean success, String message, List<Error> errorsList) {
        this.success = success;
        this.message = message;
        this.errorsList = errorsList;
    }

    public Result() {

    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
