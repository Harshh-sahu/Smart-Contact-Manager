console.log("This is script file");
console.log("This is script file");

const search = () => {
	let query = $("#search-input").val();

	if (query === "") {
		$(".search-result").hide();
	} else {
		console.log(query);

		//sending request to server

		let url = `http://localhost:8282/search/${query}`;

		fetch(url)
			.then((response) => {
				return response.json();
			})
			.then((data) => {
				console.log(data);

				let text = `<div class='list-group'>`;

				data.forEach((contact) => {
					text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;
				});
				text += `</div>`;

				$(".search-result").html(text);
				$(".search-result").show();
			});
	}
};

//first request to server to create order
const paymentStart = () => {
	console.log("payment started");
	let amount = $("#payment_field").val();
	console.log(amount);
	if (amount == "" || amount == null) {
		// alert("amount is required !!");
		swal("Failed!", "amount is requires !", "error");

		return;
	}

	//we willuse ajax send request tp server to create

	$.ajax({
		url: "/user/create_order",
		data: JSON.stringify({ amount: amount, info: "order_request" }),
		contentType: "application/json",
		type: "POST",
		dataType: "json",
		success: function(response) {
			// invoked where success
			console.log(response);
			if (response.status == "created") {
				//openn payment form
				let options = {
					key: "",
					amount: response.amount,
					currency: "INR",
					name: "smart contact manager",
					description: "Donation",
					image:
						"https://www.learncodewithdurgesh.com/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Flcwd_logo.45da3818.png&w=640&q=75",
					order_id: response.id,

					handler: function(response) {
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature),
							console.log("payment successful"),


							updatePaymentOnServer(response.razorpay_payment_id, response.razorpay_order_id, "paid");
					},
					prefill: {
						name: "",
						email: "",
						contact: "",
					},
					notes: {
						address: "Learn code with harsh",
					},
					theme: {
						color: "#3399cc",
					},
				};

				let rzp = new Razorpay(options);
				rzp.on('payment.failed', function(response) {
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.code);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					alert("OOPS PAYMENT FAILED");
					swal("Failed!", "Oops payment failed !", "error");


				});
				rzp.open();

			}
		},
		error: function(error) {
			//invoke error
			alert("something went wrong");
		},
	});
};

function updatePaymentOnServer(payment_id, order_id, status) {
	$.ajax({
		url: "/user/update_order",
		data: JSON.stringify({ payment_id: payment_id, order_id: order_id, status: status }),
		contentType: "application/json",
		type: "POST",
		dataType: "json",
		success: function(response) {
			swal("Good job!", "Payment successful!", "success");
		},
		error: function(error) {
			swal("Failed!", "Your payment is successful but we did not get on server, we will contact you ASAP!", "error");
		},
	});
};
