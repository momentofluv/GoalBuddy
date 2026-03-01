(function () {
  'use strict';

  const BOTTOM_NAV_URL = 'components/bottom-nav.html';

  // 날짜 및 캘린더 관련 로직

  // 날짜 클릭 시 해당 날짜 정보를 쿼리 파라미터로 가지고 페이지 이동 (컨트롤러 호출)
    window.viewDate = function(dateStr) {
        // 예) /today?date=2026-02-21 로 이동
        location.href = '/today?date=' + dateStr;
    };

  // 달력 출력
  function renderCalendar(year, month) {
    const grid = document.getElementById('calendarGrid');
    if (!grid) return;

    const realToday = new Date();
    const realYear = realToday.getFullYear();
    const realMonth = realToday.getMonth();
    const realDay = realToday.getDate();
    // 실제 오늘 날짜 YYYY-MM-DD 형식으로 변경
    const realTodayStr = realYear + '-' + String(realMonth + 1).padStart(2, '0') + '-' + String(realDay).padStart(2, '0');

    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay();
    const totalCells = Math.ceil((firstDay + daysInMonth) / 7) * 7;

    let html = '';
    let day = 1;

    for (let i = 0; i < totalCells; i++) {
      if (i < firstDay || day > daysInMonth) {
        html += '<span class="calendar-day empty"></span>';
      } else {

            // 날짜에 대한 이름표 생성? 날짜에 표시할 수 있도록 체크
            const dateStr = year + '-' + String(month + 1).padStart(2, '0') + '-' + String(day).padStart(2, '0');

            // 실제 판별 로직
            var isRealToday = (dateStr === realTodayStr) // 실제 오늘 날짜 표시
            var isSelected = (dateStr === serverDate); // 사용자가 선택한 날짜 표시

            html += '<button type="button" class="calendar-day' +
                                        (isRealToday ? ' today' : '') +
                                        (isSelected ? ' selected' : '') + '"' +
                                        ' onclick="viewDate(\'' + dateStr + '\')">' + day + '</button>';
            day++;
      }
    }

    grid.innerHTML = html;
  }

  function updateDateHeader(date) {
    var options = { weekday: 'long', month: 'long', day: 'numeric' };
    var formatted = date.toLocaleDateString('ko-KR', options);

    var titleEl = document.querySelector('.actual-date-text');
    if (titleEl) titleEl.textContent = formatted;
  }

  function updateCalendarTitle(year, month) {
    var months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    var titleEl = document.querySelector('.calendar-title');
    if (titleEl) titleEl.textContent = months[month] + ' ' + year;
  }

  // 목표 관리 로직 (등록, 수정, 삭제, 완료)

  // 목표 등록 시 토스트 메시지 출력
  function showToast(message) {
    const container = document.getElementById('toastContainer');
    if (!container) return; // 오류 방지

    const toast = document.createElement('div');
    toast.classList.add('toast');
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
  }

  // 체크박스 체크 및 isDone 연동
  window.toggleGoal = function(goalId) {
      const checkbox = event.target;
      const isDone = checkbox.checked;
      const goalContent = checkbox.closest('.goal-item').querySelector('.goal-content');

      // CSRF 토큰 준비
      const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      fetch(`/api/goals/${goalId}/done`, {
          method: 'PATCH',
          headers: {
              'Content-Type': 'application/json',
              [header]: token
          },
          body: JSON.stringify({ isDone: isDone })
      })
      .then(res => {
          if (res.ok) {
              // 1. 텍스트에 취소선 토글
              if (isDone) {
                  goalContent.classList.add('completed');
              } else {
                  goalContent.classList.remove('completed');
              }

              // 2. 상단 숫자 (doneCount/totalCount) 업데이트
              updateGoalCount();

          } else {
              // 실패 시 체크박스 상태 원복
              checkbox.checked = !isDone;
              showToast('상태 변경에 실패했습니다.');
          }
      })
      .catch(err => {
          checkbox.checked = !isDone;
          console.error('Error:', err);
      });
  }

  // 상단 숫자를 새로고침 없이 업데이트하는 함수
  function updateGoalCount() {
      const totalGoals = document.querySelectorAll('.goal-item').length;
      const doneGoals = document.querySelectorAll('.goal-item input[type="checkbox"]:checked').length;

      // goals-title 내부의 숫자를 찾아 변경 (기존 HTML 구조에 맞춰 클래스나 ID 지정 필요)
      const countDisplay = document.querySelector('.goals-title');
      if (countDisplay) {
          countDisplay.innerHTML = `Daily Goals (${doneGoals}/${totalGoals})`;
      }
  }

  // 목표 수정
  window.editGoal = function(id, oldContent) {
      const newContent = prompt("목표를 수정하세요:", oldContent);
      if (!newContent || newContent.trim() === "" || newContent === oldContent) return;

      const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      fetch(`/api/goals/${id}`, {
          method: 'PUT',
          headers: {
              'Content-Type': 'application/json',
              [header]: token
          },
          body: JSON.stringify({ content: newContent })
      }).then(res => {
        if (res.ok) {
            localStorage.setItem('toastMessage', '수정 완료!');
            location.reload();
        } else {
            showToast("수정에 실패했습니다.");
        }
      });
  };

  // 목표 삭제
  window.deleteGoal = function(id) {
      const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      fetch(`/api/goals/${id}`, {
          method: 'DELETE',
          headers: { [header]: token }
      }).then(res => {
          if (res.ok) {
            // 메시지를 저장소에 기록하고 새로고침 (화면에서 목표 사라지고 나서 toast 뜨도록)
            localStorage.setItem('toastMessage', '삭제 완료!');
            location.reload();
          } else {
            showToast("삭제에 실패했습니다.")
          }
      });
  };

  window.handleEdit = function(button) {
      const id = button.getAttribute('data-id');
      const content = button.getAttribute('data-content');
      editGoal(id, content);
  };

  function checkPendingToast() {
      const message = localStorage.getItem('toastMessage');
      if (message) {
          showToast(message); // 기존에 만든 showToast 함수 호출
          localStorage.removeItem('toastMessage'); // 띄운 후에는 삭제
      }
  }

  // 버튼 클릭 시 데이터를 읽어와서 삭제 함수 실행
  window.handleDelete = function(button) {
      const id = button.getAttribute('data-id');
      deleteGoal(id);
  };



  var now = new Date();
  var state = {
    year: now.getFullYear(),
    month: now.getMonth(),
    todayDate: now.getDate()
  };

  function init() {
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
              const myTab = placeholder.querySelector('[data-page="today"]');
              if (myTab) myTab.classList.add('active');
            })
            .catch(err => console.error("네비바 로드 실패:", err));
        }

    // 캘린더에 표시할 날짜 결정 (서버로부터 받은 날짜 있으면 해당 날짜, 없으면 오늘)
    var displayDate = serverDate ? new Date(serverDate) : now;

    renderCalendar(state.year, state.month);
    updateDateHeader(displayDate);
    updateCalendarTitle(state.year, state.month);
    checkPendingToast();

    document.querySelector('.calendar-card')?.addEventListener('click', function (e) {
      var btn = e.target.closest('.btn-icon');
      if (!btn) return;
      if (btn.getAttribute('aria-label') === '이전 달') {
        state.month--;
        if (state.month < 0) {
          state.month = 11;
          state.year--;
        }
      } else if (btn.getAttribute('aria-label') === '다음 달') {
        state.month++;
        if (state.month > 11) {
          state.month = 0;
          state.year++;
        }
      }
      renderCalendar(state.year, state.month, state.todayDate);
      updateCalendarTitle(state.year, state.month);
    });

    // 목표 등록 관련
    document.querySelector('.btn-add-goal')?.addEventListener('click', function () {

      if(isReadOnly) { // isReadOnly는 today.html에서 전달
        showToast("과거 날짜의 목표는 수정할 수 없습니다.");
        return;
      }

      var input = document.getElementById('goalInput');
      var text = (input && input.value || '').trim();
      if (!text) return;

      const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      fetch('/api/goals', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header] : token //csrf 토큰 추가
        },
        body: JSON.stringify({ content: text })
      })
      .then(res => {
        if (res.ok) {
            localStorage.setItem('toastMessage', '오늘의 Goal 등록 완료!');
            location.reload();
        } else if (res.status == 400) { // bad request -> 3개 초과하는 목표 등록 시도 등
            res.text().then(msg => showToast(msg));
        } else {
            showToast('문제가 발생했습니다. 다시 시도해 주세요.');
        }
      })

      if (text) {
        console.log('Add goal:', text);
        if (input) input.value = '';
      }
    });

    document.getElementById('goalInput')?.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        document.querySelector('.btn-add-goal')?.click();
      }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
