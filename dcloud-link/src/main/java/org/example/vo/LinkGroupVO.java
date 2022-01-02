package org.example.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author 二当家小D
 * @since 2021-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LinkGroupVO implements Serializable {


    private Long id;

    /**
     * 组名
     */
    private String title;

    /**
     * 账号唯一编号
     */
    private Long accountNo;

    private Date gmtCreate;

    private Date gmtModified;


}
