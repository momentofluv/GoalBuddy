(function () {
  'use strict';

  // 설정값
  const BOTTOM_NAV_URL = 'components/bottom-nav.html';
  const PLACEHOLDER_ID = 'bottomNavPlaceholder';

  function loadBottomNav() {
    var placeholder = document.getElementById(PLACEHOLDER_ID);
    if (!placeholder) return;

    fetch(BOTTOM_NAV_URL)
      .then(function (res) {
        if (!res.ok) throw new Error('Failed to load');
        return res.text();
      })
      .then(function (html) {
        placeholder.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
        var todayLink = placeholder.querySelector('[data-page="my"]');
        if (todayLink) todayLink.classList.add('active');
      })
      .catch(function () {
        placeholder.innerHTML = BOTTOM_NAV_FALLBACK;
      });
  }

  /**
   * 2. 친구 관리 섹션 토글 (접기/펴기)
   */
  function initFriendToggle() {
    const toggleHeader = document.querySelector('[data-fm-toggle]');
    if (!toggleHeader) return;

    toggleHeader.addEventListener('click', function (e) {
      // "Add Friend" 버튼 클릭 시에는 토글되지 않도록 방지
      if (e.target.closest('[data-open-add-friend]')) return;

      this.classList.toggle('friend-mgmt--open');
    });
  }

  /**
   * 3. 모달 제어 (열기/닫기)
   */
  function initModal() {
    const modal = document.getElementById('addFriendModal');
    const openBtn = document.querySelector('[data-open-add-friend]');
    const searchInput = document.getElementById('friend-search-input');

    if (!modal || !openBtn) return;

    // 모달 열기
    openBtn.addEventListener('click', (e) => {
      e.stopPropagation(); // 부모 토글 이벤트 전파 방지
      modal.classList.add('modal--open');
      modal.setAttribute('aria-hidden', 'false');
      if (searchInput) {
        setTimeout(() => searchInput.focus(), 100);
      }
    });

    // 전역 함수로 등록된 closeModal과 연동하기 위해 클래스 제거 로직
    window.closeModal = function() {
      modal.classList.remove('modal--open');
      modal.setAttribute('aria-hidden', 'true');
    };
  }

  // 초기화 실행
  function init() {
    loadBottomNav();
    initFriendToggle();
    initModal();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

/**
 * 4. 전역 API 함수 (HTML의 onclick 속성 대응)
 */
function addFriend(nickname) {
  if (!confirm(`${nickname}님을 친구로 추가하시겠습니까?`)) return;

  fetch('/api/friends/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(nickname)
  })
    .then(async res => {
      if (res.ok) {
        alert("친구 추가 완료!");
        location.reload();
      } else {
        const errorMsg = await res.text();
        alert(errorMsg || "추가 실패");
      }
    })
    .catch(() => alert("서버 오류가 발생했습니다."));
}