<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Character Search</title>
</head>
<body>
    <h1>캐릭터 검색</h1>
    <form action="/search" method="post">
        <input type="text" name="characterName" placeholder="캐릭터 이름을 입력하세요">
        <button type="submit">검색</button>
    </form>
</body>
</html>