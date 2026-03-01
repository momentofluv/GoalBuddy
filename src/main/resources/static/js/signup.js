document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('signupForm');
  const privacyCheckbox = document.getElementById('privacy');
  const passwordInput = document.getElementById('password');
  const passwordCheckInput = document.getElementById('passwordCheck');
  const matchMessage = document.getElementById('matchMessage');

  // 개인정보 처리방침 출력
  const modal = document.getElementById("privacyPolicy");
  const btn = document.getElementById("openPrivacy");
  const closeBtn = document.querySelector(".close-btn");

  // 입력된 두 비밀번호 일치하는지 확인
  function checkMatch() {
      if (!passwordCheckInput.value === "") {
          matchMessage.textContent = "";
          return;
      }
      if (passwordInput.value === passwordCheckInput.value) {
          matchMessage.textContent = "비밀번호가 일치합니다.";
          matchMessage.className = "message success";
      } else {
          matchMessage.textContent = "비밀번호가 일치하지 않습니다.";
          matchMessage.className = "message error";
      }
  }

  passwordInput.addEventListener('input', checkMatch);
  passwordCheckInput.addEventListener('input', checkMatch);

  // 개인정보 처리방침 모달
  if (btn && modal && closeBtn) { // 요소가 존재하는지 먼저 확인
    btn.addEventListener('click', () => {
      modal.style.display = "block";
    });

    closeBtn.addEventListener('click', () => {
      modal.style.display = "none";
    });

    window.addEventListener('click', (event) => {
      if (event.target === modal) {
        modal.style.display = "none";
      }
    });
  }

  // 폼 제출 유효성 검사

  if (!form || !privacyCheckbox) return;

  form.addEventListener('submit', function(event) {
    // 비밀번호가 일치하지 않으면 제출 방지
    if (newPassword.value !== confirmPassword.value) {
       event.preventDefault();
       showToast("비밀번호가 일치하지 않습니다.", "error");
       confirmPassword.focus();
       return;
    }

    // 체크박스 미동의 시 제출을 막고 알림을 띄움
    if (!privacyCheckbox.checked) {
      event.preventDefault(); // 서버 전송 중단
      showToast('개인정보 처리방침에 동의해 주세요.', 'error');
      privacyCheckbox.focus();
    }
  });
});

// 공통 토스트 함수
function showToast(message, type = 'error') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}