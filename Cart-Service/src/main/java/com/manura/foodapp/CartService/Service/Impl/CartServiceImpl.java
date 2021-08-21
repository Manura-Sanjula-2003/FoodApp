package com.manura.foodapp.CartService.Service.Impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manura.foodapp.CartService.Service.CartService;
import com.manura.foodapp.CartService.Table.CartTable;
import com.manura.foodapp.CartService.Table.FoodTable;
import com.manura.foodapp.CartService.Table.UserTable;
import com.manura.foodapp.CartService.Table.Dto.CartDto;
import com.manura.foodapp.CartService.repo.CartRepo;
import com.manura.foodapp.CartService.repo.FoodRepo;
import com.manura.foodapp.CartService.repo.UserRepo;
import com.manura.foodapp.CartService.utils.ErrorMessages;
import com.manura.foodapp.CartService.utils.Utils;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.manura.foodapp.CartService.Controller.Req.Model.CartReq;
import com.manura.foodapp.CartService.Error.Model.CartSerivceError;
import com.manura.foodapp.CartService.Error.Model.CartSerivceNotFoundError;

/**
 * @author manurasanjula
 *
 */
@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private FoodRepo foodRepo;

	private ModelMapper modelMapper = new ModelMapper();

	@Autowired
	private Utils utils;

	@Override
	public Mono<CartDto> saveCart(Mono<CartReq> cartReq) {
		return cartReq.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic())
				.switchIfEmpty(Mono.error(new CartSerivceError(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage())))
				.mapNotNull(i -> {
					if (i.getFood() == null || i.getUser() == null) {
						throw new CartSerivceError(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
					}
					return foodRepo.findByPublicId(i.getFood()).publishOn(Schedulers.boundedElastic())
							.subscribeOn(Schedulers.boundedElastic())
							.switchIfEmpty(Mono.error(new CartSerivceNotFoundError(
									ErrorMessages.NO_RECORD_FOUND.getErrorMessage())))
							.mapNotNull(food -> {
								return userRepo.findByPublicId(i.getUser()).publishOn(Schedulers.boundedElastic())
										.subscribeOn(Schedulers.boundedElastic())
										.switchIfEmpty(Mono.error(new CartSerivceNotFoundError(
												ErrorMessages.NO_RECORD_FOUND.getErrorMessage())))
										.mapNotNull(user -> {
											 CartTable cartTable = cartRepo.findByFood(food).publishOn(Schedulers.boundedElastic())
													.subscribeOn(Schedulers.boundedElastic()).block();
											 if (cartTable == null) {
													CartTable cart = new CartTable();
													cart.setFood(food);
													cart.setUser(user);
													cart.setPublicId(utils.generateId(40));
													cart.setCount(i.getCount());
													cart.setPrice((i.getCount() *food.getPrice()));
													return cart;
												} else {
													cartTable.setCount((cartTable.getCount() + 1));
													cartTable.setPrice((food.getPrice() * cartTable.getCount()));
													return cartTable;
												}
										}).flatMap(cartRepo::save)
										.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic())
										.map(e -> modelMapper.map(e, CartDto.class));
							}).flatMap(j -> j);
				}).flatMap(k -> k).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<Void> deleteCart(String id) {
		return null;
	}

	@Override
	@Transactional
	public Mono<UserTable> saveUser(Mono<UserTable> user) {
		try {
			return user.flatMap(userRepo::save).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic());
		} catch (Exception e) {
			return Mono.empty();
		}
	}

	@Override
	@Transactional
	public Mono<UserTable> updateUser(String id, Mono<UserTable> user) {
		try {
			return userRepo.findByPublicId(id).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic()).mapNotNull(usr -> {
						return user.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic())
								.mapNotNull(i -> {
									i.setId(usr.getId());
									i.setPublicId(usr.getPublicId());
									return i;
								}).flatMap(userRepo::save).publishOn(Schedulers.boundedElastic())
								.subscribeOn(Schedulers.boundedElastic());
					}).flatMap(i -> i).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic())
					.switchIfEmpty(saveUser(user));

		} catch (Exception e) {
			return Mono.empty();
		}
	}

	@Override
	public Mono<FoodTable> saveFood(Mono<FoodTable> food) {
		return food.flatMap(foodRepo::save).publishOn(Schedulers.boundedElastic())
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<FoodTable> updateFood(String id, Mono<FoodTable> food) {
		return foodRepo.findByPublicId(id).publishOn(Schedulers.boundedElastic())
				.subscribeOn(Schedulers.boundedElastic()).map(i -> {
					return food.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic())
							.mapNotNull(req -> {
								req.setId(i.getId());
								req.setPublicId(i.getPublicId());
								return req;
							}).flatMap(foodRepo::save).publishOn(Schedulers.boundedElastic())
							.subscribeOn(Schedulers.boundedElastic());
				}).flatMap(i -> i).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic())
				.switchIfEmpty(saveFood(food));
	}

}