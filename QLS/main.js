document.addEventListener("DOMContentLoaded", function () {
    // Bước 1: chỉ gọi API products để kiểm tra kết nối backend.
    fetchBooks();
    fetchUsers();
    fetchCategories().then(() => populateCategoryDropdown());

    document.getElementById("btnAddBook").addEventListener("click", addBook);
    document.getElementById("btnAddUser").addEventListener("click", addUser);
    document.getElementById("btnAddCategory").addEventListener("click", addCategory);
});

// Lưu danh sách danh mục để sử dụng
let categories = [];
const API_BASE_URL = "http://localhost:8080";

/* ---------------- SÁCH (PRODUCTS) ---------------- */

function fetchBooks() {
    return fetch(`${API_BASE_URL}/api/products`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy sản phẩm: HTTP ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("Dữ liệu sản phẩm từ API:", data);
            displayBooks(data);
        })
        .catch(error => {
            console.error("Lỗi khi lấy sản phẩm:", error);
            const bookList = document.getElementById("bookList");
            bookList.innerHTML = `<tr><td colspan="6" class="text-danger">${error.message}</td></tr>`;
        });
}

function displayBooks(books) {
    const bookList = document.getElementById("bookList");
    bookList.innerHTML = "";

    if (!Array.isArray(books) || books.length === 0) {
        bookList.innerHTML = '<tr><td colspan="6">Chưa có sản phẩm</td></tr>';
        return;
    }

    books.forEach(book => {
        const categoryName = book.category?.name || "Không có danh mục";
        const safeName = (book.name ?? "").replace(/'/g, "\\'");
        const safeDescription = (book.description ?? "").replace(/'/g, "\\'");
        const categoryId = book.category?.id ?? "null";
        
        bookList.innerHTML += `
            <tr>
                <td>${book.id}</td>
                <td>${book.name}</td>
                <td>${book.price}</td>
                <td>${book.description}</td>
                <td>${categoryName}</td>
                <td>
                    <button class="btn btn-warning" onclick="editBook(${book.id}, '${safeName}', ${book.price}, '${safeDescription}', ${categoryId})">Sửa</button>
                    <button class="btn btn-danger" onclick="deleteBook(${book.id})">Xóa</button>
                </td>
            </tr>`;
    });
}

function addBook() {
    const bookId = document.getElementById("bookId").value;
    const name = document.getElementById("bookName").value;
    const price = document.getElementById("bookPrice").value;
    const description = document.getElementById("bookDescription").value;
    const categoryId = document.getElementById("bookCategory").value;

    if (!name || !price) {
        console.error("Tên sách và giá là bắt buộc!");
        return;
    }

    // Backend Product API nhận category dưới dạng object lồng: { category: { id } }
    const bookData = {
        id: bookId ? parseInt(bookId) : undefined,
        name: name,
        price: parseFloat(price),
        description: description,
        category: categoryId ? { id: parseInt(categoryId) } : null
    };

    console.log("Dữ liệu gửi lên API:", bookData); // Debug dữ liệu gửi lên

    // Nếu có bookId thì dùng PUT (sửa), ngược lại dùng POST (thêm mới)
    const url = bookId ? `${API_BASE_URL}/api/products/${bookId}` : `${API_BASE_URL}/api/products`;
    const method = bookId ? "PUT" : "POST";

    fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bookData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`Lỗi khi ${method === "PUT" ? "cập nhật" : "thêm"} sách: ${response.status} - ${text || "Không có chi tiết lỗi"}`);
            });
        }
        return response.text().then(text => {
            return text ? JSON.parse(text) : {};
        });
    })
    .then(() => {
        resetBookForm();
        fetchBooks();
    })
    .catch(error => console.error("Lỗi khi thêm/cập nhật sách:", error.message));
}

function deleteBook(id) {
    // Xóa sản phẩm theo id trên API Spring Boot
    fetch(`${API_BASE_URL}/api/products/${id}`, { method: "DELETE" })
        .then(response => {
            if (!response.ok) throw new Error("Lỗi khi xóa sách");
            resetBookForm();
            fetchBooks();
        })
        .catch(error => console.error("Lỗi khi xóa sách:", error));
}

function editBook(id, name, price, description, categoryId) {
    document.getElementById("bookId").value = id;
    document.getElementById("bookName").value = name;
    document.getElementById("bookPrice").value = price;
    document.getElementById("bookDescription").value = description;
    document.getElementById("bookCategory").value = categoryId || "";
}

function resetBookForm() {
    document.getElementById("bookId").value = "";
    document.getElementById("bookName").value = "";
    document.getElementById("bookPrice").value = "";
    document.getElementById("bookDescription").value = "";
    document.getElementById("bookCategory").value = "";
}

// Điền danh sách danh mục vào dropdown
function populateCategoryDropdown() {
    const dropdown = document.getElementById("bookCategory");
    dropdown.innerHTML = '<option value="">Chọn danh mục</option>';
    categories.forEach(category => {
        dropdown.innerHTML += `<option value="${category.id}">${category.name}</option>`;
    });
}

/* ---------------- NGƯỜI DÙNG (USERS) ---------------- */

function fetchUsers() {
    return fetch(`${API_BASE_URL}/api/users`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy người dùng: HTTP ${response.status}`);
            }
            return response.json();
        })
        .then(data => displayUsers(data))
        .catch(error => {
            console.error("Lỗi khi lấy người dùng:", error);
            const userList = document.getElementById("userList");
            userList.innerHTML = `<tr><td colspan="9" class="text-danger">${error.message}</td></tr>`;
        });
}

function displayUsers(users) {
    const userList = document.getElementById("userList");
    userList.innerHTML = "";
    users.forEach(user => {
        const dateOfBirth = user.dateOfBirth ? new Date(user.dateOfBirth).toLocaleDateString() : "";
        const createdAt = user.createdAt ? new Date(user.createdAt).toLocaleString() : "";
        
        userList.innerHTML += `
            <tr>
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td>${user.role}</td>
                <td>${dateOfBirth}</td>
                <td>${user.address || ""}</td>
                <td>${user.phoneNumber || ""}</td>
                <td>${createdAt}</td>
                <td>
                    <button class="btn btn-warning" onclick="editUser(${user.id}, '${user.name}', '${user.email}', '${user.role}', '${user.dateOfBirth}', '${user.address}', '${user.phoneNumber}')">Sửa</button>
                    <button class="btn btn-danger" onclick="deleteUser(${user.id})">Xóa</button>
                </td>
            </tr>`;
    });
}

function addUser() {
    const userId = document.getElementById("userId").value;
    const name = document.getElementById("userName").value;
    const email = document.getElementById("userEmail").value;
    const password = document.getElementById("userPassword").value;
    const role = document.getElementById("userRole").value;
    const dateOfBirth = document.getElementById("userDateOfBirth").value;
    const address = document.getElementById("userAddress").value;
    const phoneNumber = document.getElementById("userPhoneNumber").value;

    // Kiểm tra các trường bắt buộc
    if (!name || !email) {
        console.error("Tên và email là bắt buộc!");
        return;
    }

    // Yêu cầu password khi thêm mới
    if (!userId && !password) {
        console.error("Mật khẩu là bắt buộc khi thêm người dùng mới!");
        return;
    }

    // Khi sửa mà để trống password thì backend sẽ giữ nguyên mật khẩu cũ.
    const finalPassword = password || undefined;

    const userData = {
        id: userId ? parseInt(userId) : undefined,
        name: name,
        email: email,
        password: finalPassword,
        role: role,
        dateOfBirth: dateOfBirth || null,
        address: address || null,
        phoneNumber: phoneNumber || null
    };

    const url = userId ? `${API_BASE_URL}/api/users/${userId}` : `${API_BASE_URL}/api/users`;
    const method = userId ? "PUT" : "POST";

    fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`Lỗi khi ${method === "PUT" ? "cập nhật" : "thêm"} người dùng: ${response.status} - ${text || "Không có chi tiết lỗi"}`);
            });
        }
        return response.text().then(text => {
            return text ? JSON.parse(text) : {};
        });
    })
    .then(() => {
        resetUserForm();
        fetchUsers();
    })
    .catch(error => console.error("Lỗi khi thêm/cập nhật người dùng:", error.message));
}

function deleteUser(id) {
    fetch(`${API_BASE_URL}/api/users/${id}`, { method: "DELETE" })
        .then(response => {
            if (!response.ok) throw new Error("Lỗi khi xóa người dùng");
            fetchUsers();
        })
        .catch(error => console.error("Lỗi khi xóa người dùng:", error));
}

function editUser(id, name, email, role, dateOfBirth, address, phoneNumber) {
    document.getElementById("userId").value = id;
    document.getElementById("userName").value = name;
    document.getElementById("userEmail").value = email;
    document.getElementById("userPassword").value = ""; // Để trống password khi chỉnh sửa
    document.getElementById("userRole").value = role;
    document.getElementById("userDateOfBirth").value = dateOfBirth ? new Date(dateOfBirth).toISOString().split("T")[0] : "";
    document.getElementById("userAddress").value = address || "";
    document.getElementById("userPhoneNumber").value = phoneNumber || "";
}

function resetUserForm() {
    document.getElementById("userId").value = "";
    document.getElementById("userName").value = "";
    document.getElementById("userEmail").value = "";
    document.getElementById("userPassword").value = "";
    document.getElementById("userRole").value = "";
    document.getElementById("userDateOfBirth").value = "";
    document.getElementById("userAddress").value = "";
    document.getElementById("userPhoneNumber").value = "";
}

/* ---------------- DANH MỤC (CATEGORIES) ---------------- */

function fetchCategories() {
    return fetch(`${API_BASE_URL}/api/categories`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể lấy danh mục: HTTP ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            categories = data; // Cập nhật danh sách danh mục
            console.log("Danh sách danh mục:", categories); // Debug danh mục
            displayCategories(data);
            populateCategoryDropdown(); // Cập nhật dropdown khi danh mục thay đổi
        })
        .catch(error => {
            console.error("Lỗi khi lấy danh mục:", error);
            const categoryList = document.getElementById("categoryList");
            categoryList.innerHTML = `<tr><td colspan="4" class="text-danger">${error.message}</td></tr>`;
        });
}

function displayCategories(categories) {
    const categoryList = document.getElementById("categoryList");
    categoryList.innerHTML = "";
    categories.forEach(category => {
        categoryList.innerHTML += `
            <tr>
                <td>${category.id}</td>
                <td>${category.name}</td>
                <td>${category.description}</td>
                <td>
                    <button class="btn btn-warning" onclick="editCategory(${category.id}, '${category.name}', '${category.description}')">Sửa</button>
                    <button class="btn btn-danger" onclick="deleteCategory(${category.id})">Xóa</button>
                </td>
            </tr>`;
    });
}

function addCategory() {
    const categoryId = document.getElementById("categoryId").value;
    const name = document.getElementById("categoryName").value;
    const description = document.getElementById("categoryDescription").value;

    if (!name) {
        console.error("Tên danh mục là bắt buộc!");
        return;
    }

    const categoryData = {
        id: categoryId ? parseInt(categoryId) : undefined,
        name: name,
        description: description
    };

    const url = categoryId ? `${API_BASE_URL}/api/categories/${categoryId}` : `${API_BASE_URL}/api/categories`;
    const method = categoryId ? "PUT" : "POST";

    fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(categoryData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`Lỗi khi ${method === "PUT" ? "cập nhật" : "thêm"} danh mục: ${response.status} - ${text || "Không có chi tiết lỗi"}`);
            });
        }
        return response.text().then(text => {
            return text ? JSON.parse(text) : {};
        });
    })
    .then(() => {
        resetCategoryForm();
        fetchCategories();
    })
    .catch(error => console.error("Lỗi khi thêm/cập nhật danh mục:", error.message));
}

function deleteCategory(id) {
    fetch(`${API_BASE_URL}/api/categories/${id}`, { method: "DELETE" })
        .then(response => {
            if (!response.ok) throw new Error("Lỗi khi xóa danh mục");
            fetchCategories();
        })
        .catch(error => console.error("Lỗi khi xóa danh mục:", error));
}

function editCategory(id, name, description) {
    document.getElementById("categoryId").value = id;
    document.getElementById("categoryName").value = name;
    document.getElementById("categoryDescription").value = description;
}

function resetCategoryForm() {
    document.getElementById("categoryId").value = "";
    document.getElementById("categoryName").value = "";
    document.getElementById("categoryDescription").value = "";
}