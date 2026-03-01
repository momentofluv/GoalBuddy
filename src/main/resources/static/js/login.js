document.addEventListener('DOMContentLoaded', function() {

    // 토스트 출력 함수
    function showToast(message, type = 'error') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        // type에 따라 toast success 또는 toast error 클래스가 붙음
        toast.className = `toast ${type}`;
        toast.innerHTML = message;

        container.appendChild(toast);

        // 3초 후 페이드 아웃 및 제거
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    // 로그인 실패 체크 (error-data 노드가 있는지 확인)
    const errorNode = document.getElementById('error-data');
    if (errorNode) {
        const loginErrorMsg = errorNode.dataset.message;
        if (loginErrorMsg) {
            showToast(loginErrorMsg, 'error');
        }
    }

});