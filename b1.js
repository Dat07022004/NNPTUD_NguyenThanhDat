function Cong(a,b) {
    return a + b;
}

var Cong3 = function (a,b) {
    return a + b;
}

var Cong2 = (a,b) => {
    return a + b;
}

const array = [1,2,3,4,5,6];

for (let i = 0; i < array.length; i++) {
    const element = array[i];
    console.log(element);
    
}

for (const element of array) {
    console.log(element);
}

for (const index in array) {
    console.log(array[index]); // trả ra index của phần tử khác undefined
}

let array2 = [15,2,9,13,8,10];

let result = array.sort(
    compare(-1)
);

//ép 1 hàm chỉ nhận 2 tham số phải nhận 3 tham số
function compare(type) {
    return function (a,b) {
        return type+(a-b);
    }
}

//map,filter,some,every,reduce
let result2 = array2.map(
    function (e) {
        if(e%2){
            return "le"
        }else {
            return "chan"
        }
    }
);
console.log(result2);

let student = {
    name: "Nguyen Van A",
    age: 18,
    score: [9,8,7,6,10],
    birth: {
        year: 2006,
        month: 5,
        day: 20
    },
    getAvgscore: function () {
        return this.score.reduce(
            function (total,e) {
                return total + e;
            }
        )
    }
}