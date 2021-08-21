package com.manura.foodapp.CartService.Table;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTable implements Serializable,Persistable<Integer>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8848844735978074589L;
	@Id
    private Integer id;
	private String publicId;
	private String firstName;
	private String lastName;
	private String email;
	private Boolean active;
	private Boolean emailVerify;
	private String address;
	private String pic;
	@Transient
    private boolean newUserTable;
	
	@Override
    @Transient
    public boolean isNew() {
        return this.newUserTable || id == null;
    }

    public UserTable setAsNew(){
        this.newUserTable = true;
        return this;
    }
}




