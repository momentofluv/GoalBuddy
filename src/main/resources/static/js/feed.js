(function () {
  'use strict';

  const BOTTOM_NAV_URL = 'components/bottom-nav.html';

  function init() {
    // 네비바 로드
    const placeholder = document.getElementById('bottomNavPlaceholder');
    if (placeholder) {
      fetch(BOTTOM_NAV_URL)
        .then(res => {
          if(!res.ok) throw new Error('Nav Not Found');
          return res.text();
        })
        .then(html => {
          placeholder.innerHTML = html;
          const myTab = placeholder.querySelector('[data-page="feed"]');
          if (myTab) myTab.classList.add('active');
        })
        .catch(err => console.error("네비바 로드 실패:", err));
    }

    // post-actions에서 발생하는 하트 버튼 / 좋아요 숫자 클릭 관련 처리
    document.addEventListener('click', function (e) {
      const likeBtn = e.target.closest('.like-btn');
      if (!likeBtn) return;

      // 좋아요 개수 클릭한 경우 > 모달 띄우고 종료
      if (e.target.classList.contains('action-count')) {
        e.stopPropagation();
        const targetNickname = likeBtn.getAttribute('data-nickname');
        openLikersModal(targetNickname);
        return;
      }

      // 좋아요 버튼을 클릭한 경우 > 서버와 통신
      const targetNickname = likeBtn.getAttribute('data-nickname');
      const countEl = likeBtn.querySelector('.action-count');
      const token = document.querySelector('meta[name="_csrf"]').content;
      const header = document.querySelector('meta[name="_csrf_header"]').content;

      // 서버에 JSON 형태로 좋아요 요청 전송
      fetch('/api/likes', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [header]: token
        },
        body: JSON.stringify({ targetNickname: targetNickname })
      }) // 요청자가 좋아요를 이미 눌렀는지, 해당 피드의 total 좋아요 수는 몇 개인지 응답
      .then(res => res.json())
      .then(data => {
        countEl.textContent = data.likes;
        if (data.isLiked) {
          likeBtn.classList.add('liked');
        } else {
          likeBtn.classList.remove('liked');
        }
      })
      .catch(err => console.error('좋아요 처리 중 오류:', err));
    });
  }

  // 좋아요 단 사용자 프로필 사진, 닉네임 여는 함수
  function openLikersModal(nickname) {
      const modal = document.getElementById('likersModal');
      const listContainer = document.getElementById('modal-likers-list');

      modal.classList.add('modal--open');
      listContainer.innerHTML = '<li class="loading">불러오는 중...</li>';

      // 닉네임을 기준으로 해당 게시물에 좋아요 누른 사람 조회 API
      fetch(`/api/likes/${nickname}/users`)
        .then(res => res.json())
        .then(users => {
          listContainer.innerHTML = '';
          if (users.length === 0) {
            listContainer.innerHTML = '<li>좋아요를 누른 사람이 없습니다.</li>';
            return;
          }
          users.forEach(user => {
            const li = document.createElement('li');
            li.className = 'liker-item';
            li.innerHTML = `
              <div class="f-info">
                <img src="${user.profileImageUrl || '/image/default-profile.png'}" class="mini-avatar">
                <span class="f-name">${user.nickname}</span>
              </div>
            `;
            listContainer.appendChild(li);
          });
        });
    }

    // 모달 닫기 (전역 함수)
    window.closeLikersModal = function() {
      document.getElementById('likersModal').classList.remove('modal--open');
    };

    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', init);
    } else {
      init();
    }
  })();
