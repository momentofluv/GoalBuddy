function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');

    toast.className = `toast ${type}`;
    toast.innerHTML = message;

    container.appendChild(toast);

    // 3초 뒤 자동 제거
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

document.addEventListener('DOMContentLoaded', function() {
    const resetForm = document.getElementById('resetForm');
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    const matchMessage = document.getElementById('matchMessage');
    const tokenElement = document.getElementById('token').value; // 컨트롤러에서 넘겨줄 것

    const token = tokenElement ? tokenElement.value : '';

    // 입력된 두 비밀번호 일치하는지 확인
    function checkMatch() {
        if (confirmPassword.value === "") {
            matchMessage.textContent = "";
            return;
        }
        if (newPassword.value === confirmPassword.value) {
            matchMessage.textContent = "비밀번호가 일치합니다.";
            matchMessage.className = "message success";
        } else {
            matchMessage.textContent = "비밀번호가 일치하지 않습니다.";
            matchMessage.className = "message error";
        }
    }

    newPassword.addEventListener('input', checkMatch);
    confirmPassword.addEventListener('input', checkMatch);

    // 폼 제출 시 (버튼 클릭 시)
    resetForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        // html에서 토큰 값 직접 읽어 오기
        const tokenElement = document.getElementById('token');
        const tokenValue = tokenElement ? tokenElement.value : null;

        console.log("전송 직전 토큰 확인:", tokenValue); // 토큰 전송 여부 확인 (개발ㅇㅇ)

        if (!tokenValue || tokenValue === "undefined") {
            showToast("유효하지 않은 토큰입니다.");
            return;
        }

        if (newPassword.value !== confirmPassword.value) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        if (newPassword.value.length < 8) {
            alert("비밀번호를 8자 이상 입력해주세요.");
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        try {
            const response = await fetch('/auth/reset-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken
                },
                // x-www-form-urlencoded 형식을 쉽게 만들기 위해 URLSearchParams 사용
                body: new URLSearchParams({
                    'token': tokenValue,
                    'newPassword': newPassword.value,
                    'confirmPassword': confirmPassword.value
                })
            });

            if (response.ok) {
                showToast("비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
                setTimeout(() => {
                        window.location.href = "/login";
                    }, 2000);
            } else {
                showToast("비밀번호 변경에 실패했습니다.");
            }
        } catch (error) {
            console.error("Error:", error);
            showToast("서버 통신 중 오류가 발생했습니다.");
        }
    });
});