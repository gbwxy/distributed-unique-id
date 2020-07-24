package gbwxy.distributed.id.entity;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/21
 */
public class Result {
    private Long id;
    private Status status;

    public Result() {
    }

    public Result(Long id, Status status) {
        this.id = id;
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public Status getStatus() {
        return this.status;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Result{");
        sb.append("id=").append(this.id);
        sb.append(", status=").append(this.status);
        sb.append('}');
        return sb.toString();
    }

}
