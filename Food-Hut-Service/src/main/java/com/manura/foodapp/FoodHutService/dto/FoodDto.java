package com.manura.foodapp.FoodHutService.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class FoodDto implements Serializable,Comparable<FoodDto> {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 8272833438538995040L;
	String id;
    String name;
    String description;
    String type;
    Integer unlikes;
    Integer likes;
    Double price;
    Double rating;
    Map<String, Integer> nutrition;
    String coverImage;
    List<String> images;
    Boolean offered;
    List<FoodHutDto> foodHuts;
	@Override
	public int compareTo(FoodDto o) {
		// TODO Auto-generated method stub
		return this.id.equals(o.getId()) ? 1 :0;
	}
}