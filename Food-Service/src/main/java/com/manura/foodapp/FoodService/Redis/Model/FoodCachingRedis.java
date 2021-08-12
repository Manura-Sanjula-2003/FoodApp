package com.manura.foodapp.FoodService.Redis.Model;

import java.io.Serializable;

import com.manura.foodapp.FoodService.dto.FoodDto;

import lombok.Data;

@Data
public class FoodCachingRedis implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = -4394467053305569795L;
	private String name;
	private FoodDto food;

}