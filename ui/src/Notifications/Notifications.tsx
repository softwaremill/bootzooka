import React from "react";
import Container from "react-bootstrap/Container";
import Alert from "react-bootstrap/Alert";
import { AppContext } from "../AppContext/AppContext";

const Notifications: React.FC = () => {
  const {
    state: { messages },
    dispatch,
  } = React.useContext(AppContext);

  const dismissMessage = (messageIndex: number) => {
    dispatch({ type: "REMOVE_MESSAGE", messageIndex });
  };

  return (
    <Container className="my-3">
      {messages.map(({ content, variant }, index) => (
        <Alert key={index} variant={variant} onClose={() => dismissMessage(index)} dismissible>
          {content}
        </Alert>
      ))}
    </Container>
  );
};

export default Notifications;
