package com.ict.system.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class InventoryCheckRequest {

    @NotNull(message = "资产ID不能为空")
    private Long id;

    @NotBlank(message = "位置不能为空")
    private String location;

    @NotBlank(message = "状态不能为空")
    private String status;

    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
