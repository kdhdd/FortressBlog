// 삭제 기능
const deleteButton = document.getElementById('delete-btn');

if (deleteButton) {
    deleteButton.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
        function success() {
            alert('삭제가 완료되었습니다.');
            location.replace('/articles');
        }

        function fail() {
            alert('삭제 실패했습니다.');
            location.replace('/articles');
        }

        httpRequest('DELETE', `/api/articles/${id}`, null, success, fail);
    });
}

// 수정 기능
const modifyButton = document.getElementById('modify-btn');

if (modifyButton) {
    modifyButton.addEventListener('click', event => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        const body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        });

        function success() {
            alert('수정 완료되었습니다.');
            location.replace(`/articles/${id}`);
        }

        function fail() {
            alert('수정 실패했습니다.');
            location.replace(`/articles/${id}`);
        }

        httpRequest('PUT', `/api/articles/${id}`, body, success, fail);
    });
}

// 등록 기능
const createButton = document.getElementById('create-btn');

if (createButton) {
    createButton.addEventListener('click', event => {
        const formData = new FormData();

        // JSON 데이터를 Blob으로 변환하여 articleDto 파트에 추가
        const articleDto = {
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        };
        formData.append('articleDto', new Blob([JSON.stringify(articleDto)], { type: 'application/json' }));

        // 만약 파일 첨부가 있을 경우 해당 input 요소에서 파일을 가져와 추가
        const fileInput = document.getElementById('file-input');
        if (fileInput && fileInput.files.length > 0) {
            for (let file of fileInput.files) {
                 formData.append('files', file);
            }
        }

        function success() {
            alert('등록 완료되었습니다.');
            location.replace('/articles');
        }
        function fail() {
            alert('등록 실패했습니다.');
            location.replace('/articles');
        }

        // httpRequest 함수에서 FormData인 경우 Content-Type 헤더를 설정하지 않음
        httpRequest('POST', '/api/articles', formData, success, fail);
    });
}

// 로그아웃 기능
const logoutButton = document.getElementById('logout-btn');

if (logoutButton) {
    logoutButton.addEventListener('click', event => {
        function success() {
            // 로컬 스토리지에 저장된 액세스 토큰을 삭제
            localStorage.removeItem('access_token');

            // 쿠키에 저장된 리프레시 토큰을 삭제
            deleteCookie('refresh_token');
            location.replace('/login');
        }
        function fail() {
            alert('로그아웃 실패했습니다.');
        }

        httpRequest('DELETE', '/api/refresh-token', null, success, fail);
    });
}

// 쿠키를 가져오는 함수
function getCookie(key) {
    var result = null;
    var cookie = document.cookie.split(';');
    cookie.some(function (item) {
        item = item.replace(' ', '');
        var dic = item.split('=');
        if (key === dic[0]) {
            result = dic[1];
            return true;
        }
    });
    return result;
}

// 쿠키를 삭제하는 함수
function deleteCookie(name) {
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

// HTTP 요청을 보내는 함수
function httpRequest(method, url, body, success, fail) {
    // 헤더 설정: body가 FormData이면 Content-Type 헤더를 생략해야 함
    const headers = {
        Authorization: 'Bearer ' + localStorage.getItem('access_token')
    };
    if (!(body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    fetch(url, {
        method: method,
        headers: headers,
        body: body,
    }).then(response => {
        if (response.status === 200 || response.status === 201) {
            return success();
        }
        const refresh_token = getCookie('refresh_token');
        if (response.status === 401 && refresh_token) {
            // 액세스 토큰 재발급 시도
            fetch('/api/token', {
                method: 'POST',
                headers: {
                    Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    refreshToken: getCookie('refresh_token'),
                }),
            })
                .then(res => {
                    if (res.ok) {
                        return res.json();
                    }
                })
                .then(result => {
                    // 재발급 성공 시 로컬 스토리지의 액세스 토큰을 갱신 후 재요청
                    localStorage.setItem('access_token', result.accessToken);
                    httpRequest(method, url, body, success, fail);
                })
                .catch(error => fail());
        } else {
            return fail();
        }
    });
}

// 댓글 생성 기능
const commentCreateButton = document.getElementById('comment-create-btn');

if (commentCreateButton) {
    commentCreateButton.addEventListener('click', event => {
        const articleId = document.getElementById('article-id').value;

        const body = JSON.stringify({
            articleId: articleId,
            content: document.getElementById('content').value
        });
        function success() {
            alert('등록 완료되었습니다.');
            location.replace('/articles/' + articleId);
        }
        function fail() {
            alert("등록 실패했습니다.");
            location.replace('/articles/' + articleId);
        }

        httpRequest('POST', '/api/comments', body, success, fail);
    });
}
