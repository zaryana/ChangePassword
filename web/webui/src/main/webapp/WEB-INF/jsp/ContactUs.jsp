<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Cloud-workspaces Contact Us"; %>
    <%@ include file="common/headStyle.jsp"%>
	</head>
	<body>
		<div class="MarkLayer" style="width: 100%; height: 610px;"><span></span></div>
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
		
		<div class="UIPageBody ContactPages">
			<form class="UIForm UIFormBox" action=""  method="" name="">
				<h1 class="TitleForm">Contact Us</h1>
				<table cols="2">
					<tr>
						<td class="Field">Your name:</td><td> <input class="InputText" type="text" name="" id="" value="" /></td>
					</tr>
					<tr>
						<td class="Field">Your email:</td><td> <input class="InputText" type="text" name="" id="" value="" /></td>
					</tr>
					<tr>
						<td class="Field">subject:</td><td> <input class="InputText" type="text" name="" id="" /></td>
					</tr>
					<tr>
						<td class="Field">Message:</td><td> <textarea type="text" name="" id=""></textarea></td> 
					</tr>
					<tr>
						<td class="Field"></td>
						<td> 
							<input class="Button" type="submit"  id="" value="Send" />
							<input class="Button ButtonGray" type="" id="" value="Cancel" />
						</td>
					</tr>
				</table>
			</form>
		</div>
		
		<!--begin Footer-->
    <%@ include file="common/footer.jsp"%>
	</body>
</html>
