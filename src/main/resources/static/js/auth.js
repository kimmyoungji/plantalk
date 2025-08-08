/**
 * PlantTalk 인증 관련 JavaScript - Spring Security 적용
 */
document.addEventListener('DOMContentLoaded', function() {
    // Spring Security가 로그인 처리를 담당
    // URL 파라미터에서 오류 메시지 확인
    const loginError = new URLSearchParams(window.location.search).get('error');
    if (loginError) {
        showError('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
    }
    
    // 회원가입 폼 처리 - 비밀번호 일치 확인 및 성공 시 리다이렉트
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 방지
            
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            // 비밀번호 일치 확인
            if (password !== confirmPassword) {
                showError('비밀번호가 일치하지 않습니다.');
                return;
            }
            
            // 폼 데이터 가져오기
            const formData = new FormData(registerForm);
            
            // fetch API를 사용하여 폼 데이터 전송
            fetch(registerForm.getAttribute('action') || '/api/login/register', {
                method: 'POST',
                body: formData,
                // credentials: 'same-origin' 옵션은 쿠키와 CSRF 토큰을 포함하도록 함
                credentials: 'same-origin'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // 성공 메시지 표시 후 리다이렉트
                    showSuccess('회원가입에 성공했습니다. 로그인 페이지로 이동합니다.');
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 1500);
                } else {
                    // 실패 메시지 표시
                    showError(data.message || '회원가입 중 오류가 발생했습니다.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showError('서버 오류가 발생했습니다.');
            });
        });
    }
    
    // 로그아웃 버튼 처리 - 이미 존재하는 로그아웃 폼 제출
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(event) {
            event.preventDefault();
            // 이미 HTML에 있는 로그아웃 폼 사용 (Thymeleaf가 자동으로 CSRF 토큰 추가)
            const logoutForm = document.getElementById('logoutForm');
            if (logoutForm) {
                logoutForm.submit();
            } else {
                console.error('로그아웃 폼을 찾을 수 없습니다.');
            }
        });
    }
    
    // 오류 메시지 표시
    function showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger';
        errorDiv.textContent = message;
        
        const form = document.querySelector('form');
        if (form) {
            form.insertBefore(errorDiv, form.firstChild);
            
            // 3초 후 메시지 제거
            setTimeout(() => {
                errorDiv.remove();
            }, 3000);
        }
    }
    
    // 성공 메시지 표시
    function showSuccess(message) {
        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success';
        successDiv.textContent = message;
        
        const form = document.querySelector('form');
        if (form) {
            form.insertBefore(successDiv, form.firstChild);
            
            // 3초 후 메시지 제거
            setTimeout(() => {
                successDiv.remove();
            }, 3000);
        }
    }
});
