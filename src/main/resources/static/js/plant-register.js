/**
 * PlantTalk 식물 관리 관련 JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    
    /* plantImage.addEventListener('change', function() {
        if (this.files && this.files[0]) {
            const reader = new FileReader();
            
            reader.onload = function(e) {
                imagePreview.innerHTML = `<img src="${e.target.result}" alt="식물 이미지 미리보기">`;
            };
            
            reader.readAsDataURL(this.files[0]);
        }
    }); */
    
    // 식물 유형 선택
    // const plantTypeOptions = document.querySelectorAll('.plant-type-option');
    // const plantTypeInput = document.getElementById('plantType');
    
    /* plantTypeOptions.forEach(option => {
        option.addEventListener('click', function() {
            // 이전 선택 제거
            plantTypeOptions.forEach(opt => opt.classList.remove('selected'));
            
            // 현재 선택 추가
            this.classList.add('selected');
            
            // 선택된 값 저장
            plantTypeInput.value = this.dataset.type;
        });
    }); */
    
    // 슬라이더 값 표시
    const lightLevelSlider = document.getElementById('lightLevel');
    const lightLevelValue = document.getElementById('lightLevelValue');
    const moistureSlider = document.getElementById('moisture');
    const moistureValue = document.getElementById('moistureValue');
    const temperatureSlider = document.getElementById('temperature');
    const temperatureValue = document.getElementById('temperatureValue');
    
    lightLevelSlider.addEventListener('input', function() {
        lightLevelValue.textContent = this.value + '%';
    });
    
    moistureSlider.addEventListener('input', function() {
        moistureValue.textContent = this.value + '%';
    });
    
    temperatureSlider.addEventListener('input', function() {
        temperatureValue.textContent = this.value + '°C';
    });
    
    // 식물 등록 버튼 클릭 이벤트 처리
    const plantRegisterBtn = document.getElementById('plantRegisterBtn');
    if (plantRegisterBtn) {
        plantRegisterBtn.addEventListener('click', function(event) {
            event.preventDefault(); // 기본 이벤트 방지

            // 사용자 ID 가져오기
            const userIdElement = document.getElementById('userId');
            const userId = userIdElement ? parseInt(userIdElement.value, 10) : null;
            
            // userId 값 검증
            if (!userId) {
                console.error('userId가 없거나 유효하지 않습니다:', userId);
                showError('사용자 ID가 없습니다. 로그인을 다시 해주세요.');
                return;
            }

            // 폼 데이터 수집
            const name = document.getElementById('name').value;
            const species = document.getElementById('species').value;
            
            // JSON 데이터 구성
            const jsonData = {
                name: name,
                species: species,
                userId: userId
            };
            
            // 서버에 JSON으로 전송
            fetch('/api/plant', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(jsonData)
            })
            .then(response => response.json())
            .then(data => {
                console.log('응답 데이터:', data);

                debugger;
                
                if (data.success) {

                    // 등록 성공
                    const plantId = data.data.plantId ? data.data.plantId : null;

                    // 상태 업데이트 폼이 있고 식물 ID가 있으면 상태 업데이트 실행
                    if (plantId) {
                        // plantId 설정
                        document.getElementById('plantId').value = plantId;
                        
                        // 상태 업데이트 폼 제출
                        const stateData = {
                            plantId:  document.getElementById('plantId').value,
                            lightLevel: document.getElementById('lightLevel').value,
                            moisture: document.getElementById('moisture').value,
                            temperature: document.getElementById('temperature').value,
                            touched: document.getElementById('touched').checked
                        };

                        fetch('/api/state', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(stateData)
                        })
                        .then(response => response.json())
                        .then(stateData => {
                            showSuccess('식물이 성공적으로 등록되었습니다.');
                            setTimeout(() => {
                                window.location.href = '/';
                            }, 2000);
                        })
                        .catch(error => {
                            console.error('상태 업데이트 오류:', error);
                            debugger;
                            showSuccess('식물은 등록되었지만 상태 업데이트에 실패했습니다.');
                            setTimeout(() => {
                                window.location.href = '/';
                            }, 2000);
                        });
                    } else {
                        showSuccess('식물이 성공적으로 등록되었습니다.');
                        setTimeout(() => {
                            window.location.href = '/';
                        }, 2000);
                    }
                } else {
                    // 등록 실패
                    showError(data.message || '식물 등록에 실패했습니다.');
                }
            })
            .catch(error => {
                console.error('식물 등록 중 오류 발생:', error);
                showError('서버 연결 중 오류가 발생했습니다.');
            });
        });
    }
    
    // 오류 메시지 표시
    function showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger';
        errorDiv.textContent = message;
        
        const form = document.querySelector('form');
        form.insertBefore(errorDiv, form.firstChild);
        
        // 3초 후 메시지 제거
        setTimeout(() => {
            errorDiv.remove();
        }, 3000);
    }
    
    // 성공 메시지 표시
    function showSuccess(message) {
        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success';
        successDiv.textContent = message;
        
        const form = document.querySelector('form');
        form.insertBefore(successDiv, form.firstChild);
        
        // 3초 후 메시지 제거
        setTimeout(() => {
            successDiv.remove();
        }, 3000);
    }
});
