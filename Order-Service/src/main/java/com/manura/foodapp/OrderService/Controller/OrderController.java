package com.manura.foodapp.OrderService.Controller;

import java.security.Principal;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.manura.foodapp.OrderService.Controller.Res.OperationStatusModel;
import com.manura.foodapp.OrderService.Error.Model.OrderSerivceError;
import com.manura.foodapp.OrderService.Service.Impl.OrderServiceImpl;
import com.manura.foodapp.OrderService.Service.Impl.RedisServiceImpl;
import com.manura.foodapp.OrderService.Utils.ErrorMessages;
import com.manura.foodapp.OrderService.controller.Req.BillingAndDeliveryAddressReq;
import com.manura.foodapp.OrderService.controller.Req.OrderReq;
import com.manura.foodapp.OrderService.dto.OrderDto;
import com.manura.foodapp.OrderService.dto.RefundDto;
import com.manura.foodapp.OrderService.dto.BillingAndDeliveryAddressDto;
import com.manura.foodapp.OrderService.dto.FullOrderDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
@Controller
@RequestMapping("/orders")
class PdfController{
	
	@SuppressWarnings("unused")
	private class  PdfControllerException extends RuntimeException{

		private static final long serialVersionUID = 3588856919244065687L;
		public PdfControllerException(String message) {
	        super(message);
	    }
	}

	@Autowired
	private RedisServiceImpl redisServiceImpl;
	
	
	@GetMapping(path = "/pdf/{orderId}", produces = "application/pdf")
	Mono<HttpEntity<byte[]>> getPDF(@PathVariable String orderId) {
		
	return redisServiceImpl.getPdfAsByteArray(orderId).map(i->{
			HttpHeaders header = new HttpHeaders();
		    header.setContentType(MediaType.APPLICATION_PDF);
		    header.setContentLength(i.length);
		    return  new HttpEntity<byte[]>(i, header);
		});
	}
	
	@GetMapping("/order-confrim-web")
	Mono<String> orderConfrimWeb(PdfControllerException ex){
		return Mono.just("OrderConfirmWeb")
				.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}
	
	@ExceptionHandler(PdfControllerException.class)
	Mono<String> pdfControllerException(PdfControllerException ex){
		return Mono.just("notFound")
				.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}


}

@RestController
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private OrderServiceImpl orderServiceImpl;

	@PostMapping
	public Mono<ResponseEntity<String>> saveOrder(@RequestBody Mono<OrderReq> req, Mono<Principal> principal) {
		return principal.map(Principal::getName)
				.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic()).map(usr -> {
					return req
							.switchIfEmpty(Mono.error(new OrderSerivceError(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage())))
							.map(i->{
						return orderServiceImpl.saveOrder(Mono.just(i), usr).publishOn(Schedulers.boundedElastic())
								.subscribeOn(Schedulers.boundedElastic()).map(ResponseEntity::ok);
					});
				}).flatMap(__ -> __).flatMap(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}
	
	@GetMapping("/{email}")
	public Flux<OrderDto> allOrders(@PathVariable String email, Mono<Principal> principal) {
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.getOrder(email).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic());
		}).flatMapMany(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());

	}

	@GetMapping("/{email}/{orderId}")
	public Mono<FullOrderDto> getOneOrder(@PathVariable String email, @PathVariable String orderId,
			Mono<Principal> principal) {
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.getOneOrder(email, orderId).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic());
		}).flatMap(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}
	
	@GetMapping("/order-completed/{email}/{orderId}")
	public Mono<String> orderCompleted(@PathVariable String email, @PathVariable String orderId,
			Mono<Principal> principal) {
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.orderCompleted(email, orderId).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic());
		}).flatMap(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}

	@GetMapping("/{email}/{orderId}/confirmOrder")
	public Mono<OperationStatusModel> confirmOrder(@PathVariable String email, @PathVariable String orderId,
			Mono<Principal> principal) {
		
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.confirmOrder(email, orderId).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic()).map(i->{
						if(i) {
							OperationStatusModel returnValue = new OperationStatusModel();
							returnValue.setOperationName("ORDER_CONFRIM");
							returnValue.setOperationResult("SUCCESS");
							return returnValue;
						}else {
							OperationStatusModel returnValue = new OperationStatusModel();
							returnValue.setOperationName("ORDER_CONFRIM");
							returnValue.setOperationResult("FAIL");
							return returnValue;
						}
					});
		}).flatMap(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
		 
		 
	}

	@GetMapping("/{email}/refund")
	public Flux<RefundDto> getAllRefund(@PathVariable String email, Mono<Principal> principal) {
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.getAllRefund(email).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic());
		}).flatMapMany(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}

	@PostMapping("/{email}/setNewBillingAndDeliveryAddress")
	Mono<String> setNewBillingAndDeliveryAddress(@PathVariable String email,
			@RequestBody Mono<BillingAndDeliveryAddressReq> req, Mono<Principal> principal) {
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return req.switchIfEmpty(Mono.error(new OrderSerivceError(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage()))).map(i->{
				return orderServiceImpl.setNewBillingAndDeliveryAddress(Mono.just(i),email);
			});
		}).flatMap(__ -> __).flatMap(__ -> __)
				.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic())
				;

	}
	
	@GetMapping("/{email}/changeBillingAndDeliveryAddress/{id}")
	Mono<String> changeBillingAndDeliveryAddress(@PathVariable String email, @PathVariable Long id,
			Mono<Principal> principal) {
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.changeBillingAndDeliveryAddress(user, id).publishOn(Schedulers.boundedElastic())
					.subscribeOn(Schedulers.boundedElastic());
		}).flatMap(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());

	}

	@GetMapping("/{email}/allBillingAndDeliveryAddress")
	Flux<BillingAndDeliveryAddressDto> getAllBillingAndDeliveryAddress(@PathVariable String email,
			Mono<Principal> principal) {
		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.getAllBillingAndDeliveryAddress(email);
		}).flatMapMany(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
	}

	@PostMapping("/{email}/refund")
	public Mono<RefundDto> requestARefund(@RequestPart("email") String email, @RequestPart("reason") String reason,
			@RequestPart("userId") String userId, @RequestPart("orderId") String orderId,
			@RequestPart(name = "images", required = false) Flux<FilePart> fileParts, Mono<Principal> principal) {

		return principal.map(Principal::getName).map(user -> {
			if (!user.equals(email)) {
				throw new OrderSerivceError(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());
			}
			return orderServiceImpl.requestARefund(fileParts, email, reason, userId, orderId)
					.publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());
		}).flatMap(__ -> __).publishOn(Schedulers.boundedElastic()).subscribeOn(Schedulers.boundedElastic());

	}
}