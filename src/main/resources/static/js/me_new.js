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
          // 'My' 탭 활성화
          const myTab = placeholder.querySelector('[data-page="my"]');
          if (myTab) myTab.classList.add('active');
        })
        .catch(err => console.error("네비바 로드 실패:", err));
    }

    // 프로필 사진 변경
    const cameraTrigger = document.getElementById('camera-trigger');
    const fileInput = document.getElementById('profile-file-input');     // 실제 input 버튼
    const profileImg = document.getElementById('profile-img-preview');   // 기존 사진
    const imagePlaceholder = document.getElementById('profile-placeholder');

    if (cameraTrigger && fileInput) {
      cameraTrigger.addEventListener('click', () => {
        fileInput.click(); // 카메라 아이콘 클릭 시 실제 input 버튼 클릭하도록 함
      });

      fileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
          if (!file.type.startsWith('image/')) { // 이미지 파일인지 확장자 확인
            showToast("이미지 파일만 업로드 가능합니다.");
            return;
          }

          const reader = new FileReader();
          reader.onload = function(event) {
            if (profileImg) { // 이미 이미지 보여지고 있으면 교체
              profileImg.src = event.target.result;
            } else if (imagePlaceholder) {
              // <img> 없던 경우 새롭게 생성
              imagePlaceholder.outerHTML = `<img src="${event.target.result}" id="profile-img-preview" alt="Profile"/>`;
            }

            uploadProfileImage(file);
          };
          reader.readAsDataURL(file);
        }
      });
    }

    // 친구 관리 토글
    const fmToggle = document.querySelector('[data-fm-toggle]');
    if (fmToggle) {
      fmToggle.addEventListener('click', function(e) {
        if (e.target.closest('[data-open-add-friend]')) return;
        this.classList.toggle('friend-mgmt--open');
      });
    }

    // 친구 삭제
    window.deleteFriend = function(friendId) {
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch(`/api/friends/${friendId}`, {
            method: 'DELETE',
            headers: { [header]: token }
        })
        .then(res => {
            if (res.ok) {
                showToast("친구가 삭제되었습니다.");

                // DOM 직접 조작해 제거 가능
                const targetElement = document.getElementById(`friend-${friendId}`); // HTML에서 제거할 li 요소 찾기

                if (targetElement) {
                    targetElement.remove();
                } else {
                    showToast("친구 삭제 중 문제가 발생하였습니다.")
                }
            }
        });
    };

    // 3. 모달 제어
    const modal = document.getElementById('addFriendModal');
    const openBtn = document.querySelector('[data-open-add-friend]');

    window.closeModal = function() {
      modal.classList.remove('modal--open');
    };

    if (openBtn && modal) {
      openBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        modal.classList.add('modal--open');

        const searchInput = modal.querySelector('#friend-search-input');
        if (searchInput) {
          setTimeout(() => searchInput.focus(), 100);
        }
      });
    }

      const searchInput = document.getElementById('friend-search-input');
      const modalFriendsList = document.getElementById('modal-friends-list');

      if (searchInput) {
        searchInput.addEventListener('keydown', function(e) {
          if (e.key === 'Enter') { // 엔터 감지
            const keyword = this.value.trim(); // 불필요 공백 제거

            if (keyword.length < 1) {
              alert("검색어를 입력해주세요.");
              return;
            }

            // 검색 API 호출
            fetch(`/api/friends/search?keyword=${encodeURIComponent(keyword)}`)
              .then(res => {
                if (!res.ok) throw new Error('Search Failed');
                return res.json();
              })
              .then(data => {
                // 검색 결과 렌더링 함수 호출
                renderSearchResult(data);
              })
              .catch(err => {
                console.error("검색 중 오류 발생:", err);
                modalFriendsList.innerHTML = '<li class="no-result">검색 중 오류가 발생했습니다.</li>';
              });
          }
        });
      }

      const nicknameModal = document.getElementById('updateNicknameModal');
      const openNicknameBtn = document.querySelector('[data-open-nickname-modal]');

      window.closeNicknameModal = function() {
        nicknameModal.classList.remove('modal--open');
      };

      if (openNicknameBtn && nicknameModal) {
        openNicknameBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          nicknameModal.classList.add('modal--open');
        });
      }

      const withdrawModal = document.getElementById('withdrawModal');
          window.openWithdrawModal = function() {
            if (withdrawModal) withdrawModal.classList.add('modal--open');
          };

          window.closeWithdrawModal = function() {
            if (withdrawModal) withdrawModal.classList.remove('modal--open');
          };

      // 닉네임 변경 실행
      window.submitNicknameChange = function() {
        const newNickname = document.getElementById('new-nickname-input').value.trim();
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        if (!newNickname) {
          showToast("변경할 닉네임을 입력하세요.");
          return;
        }

        fetch('/settings/update-nickname', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            [header]: token
          },
          body: JSON.stringify(newNickname)
        })
        .then(async res => {
          if (res.ok) {
            showToast("닉네임이 변경되었습니다! ✨");
            setTimeout(() => location.reload(), 1000);
          } else {
            const msg = await res.text();
            showToast(msg || "변경 실패", "error");
          }
        })
        .catch(() => showToast("서버 오류가 발생했습니다.", "error"));
      };

      const settingsToggle = document.querySelector('[data-settings-toggle]');
      if (settingsToggle) {
        settingsToggle.addEventListener('click', function(e) {
          if (e.target.closest('button')) return; // 버튼 클릭 시 토글 방지
          this.classList.toggle('settings--open');
        });
      }

      // 검색 결과를 화면에 그리는 함수
      function renderSearchResult(friends) {
        if (!modalFriendsList) return;

        modalFriendsList.innerHTML = ''; // 이전 결과 초기화

        if (friends.length === 0) {
          modalFriendsList.innerHTML = '<li class="no-result">검색 결과가 없습니다.</li>';
          return;
        }

        friends.forEach(friend => {
          const li = document.createElement('li');
          li.className = 'modal-friend-item';

          li.innerHTML = `
            <div class="f-info">
              <span class="f-name">${friend.nickname}</span>
              <span class="f-email">${friend.email}</span>
            </div>
            <button class="btn-add" onclick="addFriend('${friend.nickname}')">Add</button>
          `;
          modalFriendsList.appendChild(li);
         });
    }

    // 비밀번호 변경 메일 요청
    const resetModal = document.getElementById('confirmResetModal');
    const openResetBtn = document.getElementById('btn-reset-pw');
    const confirmResetBtn = document.getElementById('confirm-reset-btn');

    // 모달 닫기 함수
    window.closeResetModal = function() {
      resetModal.classList.remove('modal--open');
    };

    if (openResetBtn && resetModal) {
      // 버튼 클릭 시 모달 열기
      openResetBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        resetModal.classList.add('modal--open');
      });
    }

    if (confirmResetBtn) {
      // 모달 안의 '링크 발송하기' 버튼 클릭 시
      confirmResetBtn.addEventListener('click', function() {
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        confirmResetBtn.disabled = true;
        confirmResetBtn.innerText = "발송 중...";

        fetch('/settings/send-reset-link', {
          method: 'POST',
          headers: { [header]: token }
        })
        .then(async res => {
          if (res.ok) {
            showToast("패스워드 재설정 링크가 전송되었습니다.");
            closeResetModal();
          } else {
            showToast("이메일 전송에 실패했습니다.", "error");
          }
        })
        .catch(() => showToast("서버 오류가 발생했습니다.", "error"))
        .finally(() => {
          confirmResetBtn.disabled = false;
          confirmResetBtn.innerText = "링크 발송하기";
        });
      });
    }

  }

  window.addFriend = function(nickname) {
    // CSRF 토큰
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch('/api/friends/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        [header]: token
      },
      body: JSON.stringify(nickname) // 객체 형태로 전달하는 것이 일반적입니다.
    })
      .then(async res => {
        if (res.ok) {
          showToast("친구 추가 완료!");
          setTimeout(() => location.reload(), 1000);
        } else {
          showToast("이미 추가된 친구이거나 오류가 발생했습니다.");
        }
      })
      .catch(() => showToast("친구 추가 중 오류가 발생했습니다."));
  };

  // 토스트 알림 함수 정의
  function showToast(message, type = "success") {
    const toast = document.createElement('div');
    toast.className = `toast toast--${type}`;
    toast.innerHTML = message;
    document.body.appendChild(toast);

    // 3초 후 제거
    setTimeout(() => {
      toast.classList.add('toast--fade-out');
      setTimeout(() => toast.remove(), 500);
    }, 2500);
  }

  // 이미지 서버로 전송하는 함수
  function uploadProfileImage(file) {
      const formData = new FormData(); // FormData 객체 생성

      formData.append("profileImage", file); // SettingsController의 @RequestParam("profileImage")과 동일하도록

      const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      fetch('/settings/update-profile-image', {
          method: 'POST',
          headers: { [header]: token }, // Content-Type은 적지 마세요!
          body: formData // 트럭(formData)에 파일을 실어 보냅니다.
      })
      .then(res => {
          if(res.ok) showToast("사진 업데이트 완료!");
      });
  }

  document.addEventListener('DOMContentLoaded', init);
})();