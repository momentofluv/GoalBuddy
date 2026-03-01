document.addEventListener('DOMContentLoaded', () => {
    // 토스트 컨테이너 생성 (없을 경우 자동 생성)
    if (!document.getElementById('toastContainer')) {
        const container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    // 비밀번호 찾기 링크 이벤트 연결
    const forgotLink = document.querySelector('.link-forgot');
    if (forgotLink) {
        forgotLink.addEventListener('click', (e) => {
            e.preventDefault();
            openForgotModal();
        });
    }

    // 모달 외부 클릭 시 닫기
    window.onclick = (event) => {
        const modal = document.getElementById('forgotPasswordModal');
        if (event.target === modal) {
            closeForgotModal();
        }
    };

    // 탈퇴 성공 시
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.get('withdraw') === 'true') {
                if (typeof showToast === 'function') {
                    showToast("그동안 GoalBuddy를 이용해주셔서 감사합니다.<br>더 멋진 목표와 함께 다시 만나길 바랄게요!", "success");
                }
    }
});

// forgot password 요청 관련 toast 출력
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

// forgot password에서 이메일 입력하는 모달 제어
 function openForgotModal() {
     document.getElementById('forgotPasswordModal').style.display = 'flex';
 }

 function closeForgotModal() {
     document.getElementById('forgotPasswordModal').style.display = 'none';
 }

// 비밀번호 재설정 메일 발송
async function requestPasswordReset() {
    const emailInput = document.getElementById('forgotEmail');
    const email = emailInput.value.trim();
    const sendBtn = document.getElementById('sendEmailBtn');

    if (!email) {
        showToast("이메일을 입력해 주세요.");
        emailInput.focus();
        return;
    }

    // 버튼 로딩 상태 표시
    sendBtn.disabled = true;
    sendBtn.innerText = "발송 요청 중...";

    try {
        const response = await fetch('/auth/send-reset-mail', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `email=${encodeURIComponent(email)}`
        });

        if (response.ok) {
            showToast("가입한 이메일 입력 시 해당 이메일로 재설정 링크가 발송됩니다.");
            closeForgotModal();
        } else {
            showToast("메일 발송에 실패했습니다. 다시 시도해 주세요.");
        }
    } catch (error) {
        console.error("Mail Request Error:", error);
        showToast("서버 통신 중 오류가 발생했습니다.");
    } finally {
        // 버튼 상태 복구
        sendBtn.disabled = false;
        sendBtn.innerText = "링크 발송";
    }
}

// 모달 내 발송 버튼 클릭 이벤트 리스너
const sendEmailBtn = document.getElementById('sendEmailBtn');
if (sendEmailBtn) {
    sendEmailBtn.addEventListener('click', requestPasswordReset);
}

// 엔터 키 입력 시 발송 처리
const forgotEmailInput = document.getElementById('forgotEmail');
if (forgotEmailInput) {
    forgotEmailInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') requestPasswordReset();
    });
}

// login.html 하단 스크립트 추가
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('withdraw') === 'true') {
    showToast("회원 탈퇴가 정상적으로 처리되었습니다.", "success");
}

