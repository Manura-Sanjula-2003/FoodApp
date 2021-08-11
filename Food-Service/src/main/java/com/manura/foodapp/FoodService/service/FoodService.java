package com.manura.foodapp.FoodService.service;


import java.util.List;

import org.springframework.data.geo.Distance;
import org.springframework.http.codec.multipart.FilePart;

import com.manura.foodapp.FoodService.controller.Model.Res.ImageUploadingRes;
import com.manura.foodapp.FoodService.dto.CommentsDto;
import com.manura.foodapp.FoodService.dto.FoodDto;
import com.manura.foodapp.FoodService.dto.FoodHutDto;
import com.manura.foodapp.FoodService.dto.UserDto;
import com.manura.foodapp.FoodService.entity.UserEntity;
import org.springframework.data.geo.Point;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FoodService {
    Flux<FoodDto> findAll();
    Flux<CommentsDto> findAllComment(String foodId);
    Flux<FoodDto> findByTypeAndName(String type, String name);
    Flux<FoodDto> findByNames(String name);
    Flux<FoodDto> findByType(String type);
    Mono<FoodDto> findById(String id);
    Mono<FoodDto> save(Mono<FoodDto> foodDto, List<String> foodHutId);
    Mono<FoodDto> update(String id,Mono<FoodDto> foodDto,List<String> foodHutIds);
    Mono<CommentsDto> saveComment(String id,Mono<CommentsDto> comment,String user);
    Mono<FoodHutDto> saveFoodHut( Mono<FoodHutDto> foodHut);
    Mono<FoodHutDto> updateFoodHut(Mono<FoodHutDto> foodHut,String id);
    Mono<UserEntity> getUser(String id);
    Mono<UserEntity> saveUser(Mono<UserDto> user);
    Mono<UserEntity> updateUser(String id,Mono<UserDto> user);
    Mono<FoodDto> uploadCoverImage(String id,Mono<FilePart> filePartFlux);
    Mono<FoodDto> uploadImages(String id,Flux<FilePart> filePartFlux);
    Flux<ImageUploadingRes> uploadImagesMethod2(String id,Flux<FilePart> filePartFlux);
    Flux<FoodDto> findByLocationNear(Point p, Distance d);
}
