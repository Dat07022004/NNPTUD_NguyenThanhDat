// Câu 1: 
function Product(id, name, price, quantity, category, isAvailable) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.quantity = quantity;
    this.category = category;
    this.isAvailable = isAvailable;
}

// Câu 2: 
const products = [
    new Product(1, "iPhone", 35000000, 10, "Điện tử", true),
    new Product(2, "Samsung Galaxy", 28000000, 5, "Điện tử", true),
    new Product(3, "MacBook", 45000000, 3, "Điện tử", true),
    new Product(4, "AirPods", 6000000, 0, "Phụ kiện", false),
    new Product(5, "Apple Watch", 20000000, 8, "Phụ kiện", true),
    new Product(6, "iPad", 18000000, 12, "Điện tử", true),
];

console.log("List sản phẩm");
console.log(products);

// Câu 3: 
console.log("\nCâu 3: Mảng name và price");
const nameAndPrice = products.map(product => ({
    name: product.name,
    price: product.price
}));
console.log(nameAndPrice);

// Câu 4: 
console.log("\nCâu 4: Sản phẩm còn hàng trong kho");
const inStockProducts = products.filter(product => product.quantity > 0);
console.log(inStockProducts);

// Câu 5: 
console.log("\nCâu 5: Kiểm tra có sản phẩm giá trên 30000000");
const has30mProduct = products.some(product => product.price > 30000000);
console.log("Có sản phẩm giá trên 30000000:", has30mProduct);

// Câu 6: 
console.log("\nCâu 6: Kiểm tra tất cả Phụ kiện có đang bán");
const accessoriesProducts = products.filter(product => product.category === "Phụ kiện");
const allAccessoriesNow = accessoriesProducts.every(product => product.isAvailable === true);
console.log("Tất cả Phụ kiện đang được bán:", allAccessoriesNow);

// Câu 7: 
console.log("\nCâu 7: Tổng giá trị kho hàng");
const totalInventory = products.reduce((total, product) => {
    return total + (product.price * product.quantity);
}, 0);
console.log("Tổng giá trị kho hàng:", totalInventory.toLocaleString('vi-VN'), "VNĐ");

// Câu 8: 
console.log("\nCâu 8: Duyệt mảng với for-of");
for (const product of products) {
    const status = product.isAvailable ? "Đang bán" : "Ngừng bán";
    console.log(`${product.name} - ${product.category} - ${status}`);
}

// Câu 9: 
console.log("\nCâu 9: Duyệt thuộc tính với for...in");
console.log("Ví dụ với sản phẩm đầu tiên:");
for (const key in products[0]) {
    console.log(`Tên thuộc tính: ${key}`);
    console.log(`Giá trị tương ứng: ${products[0][key]}`);
}

// Câu 10:
console.log("\nCâu 10: Danh sách tên sản phẩm đang bán và còn hàng");
const availableAndInStock = products
    .filter(product => product.isAvailable && product.quantity > 0)
    .map(product => product.name);
console.log(availableAndInStock);
