INSERT INTO members(member_id,email,password,role) VALUES
(1,'user@submate.test','$2a$10$TFHGegHJxHG7Njni5IQ7JeezEGgwKJXhFzIF/q.CYvKk4.Z8inRja','USER'),
(2,'other@submate.test','$2a$10$TFHGegHJxHG7Njni5IQ7JeezEGgwKJXhFzIF/q.CYvKk4.Z8inRja','USER'),
(3,'admin@submate.test','$2a$10$TFHGegHJxHG7Njni5IQ7JeezEGgwKJXhFzIF/q.CYvKk4.Z8inRja','ADMIN');

INSERT INTO categories(category_id,name) VALUES
(1,'OTT'),(2,'생산성');

INSERT INTO products(product_id,category_id,name,price,billing_cycle,status) VALUES
(1,1,'SubMate Cinema',12900.00,'MONTHLY','ACTIVE'),
(2,2,'SubMate Workspace',99000.00,'YEARLY','ACTIVE'),
(3,1,'판매 종료 상품',5000.00,'MONTHLY','INACTIVE');