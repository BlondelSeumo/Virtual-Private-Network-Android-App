import React from "react";

const md5 = require('md5');
import {Formik} from "formik";
import {Form, Input, Modal, notification} from "antd";
import * as Yup from "yup";
import css from 'styled-jsx/css'

import {LoadingIcon} from "@app/components/core/loading-icon";
import Container from "@app/components/core/container";
import Row from "@app/components/core/row";
import Col from "@app/components/core/col";
import UIButton from "@app/components/core/button";
import {withRouter} from "react-router";
import {auth, firestore} from "@app/services/firebase";
import {LoadingPage, LoadingPage2} from "@app/components/core/loading";
import Layout from "@app/components/layout";
import BackIcon from "@app/resources/images/arrows-long-left.svg";
import moment from "moment";
import {API, POST} from "@app/request";
import {postDataWithFetch} from "@app/utils";
import {EMAIL} from "@app/configs";

const styles = css.global`
  .ant-layout-content {
    padding: 0!important;
  }
  .login {
    .breadcrumb-user {
      height: 48px;
      border: solid 1px #e0e0e0;
      background-color: #ffffff;
      padding-left: 72px;
      padding-right: 72px;
    }
    .login-content {
      min-width: 416px;
      height: fit-content;
      width: fit-content;
      border-radius: 4px;
      border: solid 1px #e0e0e0;
      background-color: #ffffff;
      padding: 16px;
      margin: auto;
      margin-top: 24px;
    }
    .title-login {
      font-size: 20px;
      font-weight: bold;
      line-height: 1.25;
      letter-spacing: normal;
      text-align: left;
      color: #2a2a2c;
      margin-bottom: 18px;
    }
    .joined-at {
      font-size: 13px;
      line-height: 1.38;
      letter-spacing: 0.3px;
      color: #2a2a2c;
      margin-top: 11px;
      margin-bottom: 15px;
    }
    .box {
      min-width: 188px;
      height: 80px;
      border-radius: 4px;
      border: solid 1px #e0e0e0;
      background-color: #ffffff;
      padding: 16px;
      .box-title {
        font-size: 8px;
        font-weight: normal;
        line-height: 1.25;
        letter-spacing: 1.28px;
        color: #9e9e9e;
      }
      .desc {
        font-size: 32px;
        font-weight: 200;
        line-height: 1.25;
        letter-spacing: normal;
        color: #2a2a2c;
        margin-top: 6px;
      }
    }
  }
`

const UserDetail = ({location, history, ...props}) => {
  const fields = {
    email: "email",
  };
  const [error, setError] = React.useState("")
  const [info, setInfo] = React.useState({})
  const [success, setSuccess] = React.useState("")
  const [isLoading, setLoading] = React.useState(false)
  const [isPageLoading, setPageLoading] = React.useState(true)
  const [isPageLoading2, setPageLoading2] = React.useState(false)
  const [initialValues, setInitialValues] = React.useState({
    [fields.email]: "",
  })

  const validationSchema = Yup.object({
    [fields.email]: Yup.string()
      .email("Invalid email")
      .required("Please enter your email")
  });

  const onHandleSubmit = async (formValues) => {
    setLoading(true)
    createAdminUser(formValues)
  };

  const createAdminUser = (formValue) => {
    firestore.collection("admin").doc(info?.email).set({
      email: formValue?.email
    })
      .then(() => {
        setLoading(false)
      })
      .catch(() => {
        setLoading(false)
        setError("Saving failed, please try again")
      })
  }

  const removeError = ({errors, name, setErrors}) => {
    const newErrors = {...errors};
    delete newErrors?.[name];
    setErrors({...newErrors});
  };

  React.useEffect(() => {
    const {
      match: {
        params: {id, type}
      }
    } = props;

    const currentUser = auth.currentUser

    if (id && type && (currentUser?.email && currentUser?.email?.toLowerCase() !== EMAIL)) {
      firestore.collection(type).doc(id).get()
        .then((result) => {
          const tmp = result?.data?.() || {}
          setInfo({
            ...tmp,
            id: result?.id
          })
          setInitialValues({
            ...initialValues,
            email: tmp?.email || result?.id || ""
          })
          setPageLoading(false)
        })
        .catch(() => {
          history.push("/users")
        })
    } else {
      history.push("/users")
    }
  }, [])

  const formatMB = (num) => {
    return ((parseFloat(num || 0)) / (1024 * 1024)).toFixed(2)
  }

  const remove = () => {
    const {
      match: {
        params: {type}
      }
    } = props;

    Modal.confirm({
      title: `Are you sure want to delete ${info?.id}`,
      onOk: () => {
        setPageLoading2(true)
        firestore.collection("users").doc(info?.id).delete().then(() => {
          if (type === "users") {
            return postDataWithFetch(`${API}removeUser`, {
              "email": info?.id
            }, {})
          } else return Promise.resolve()
        })
          .then(() => {
            notification.info({
              description: `User successfully deleted!`,
              placement: "bottomRight",
              duration: 2,
              icon: "",
              className: "core-notification info",
              onClose: () => {
                setPageLoading2(false)
                history.goBack()
              }
            });
          })
          .catch(() => {
          notification.info({
            description: `Error removing user!`,
            placement: "bottomRight",
            duration: 2,
            icon: "",
            className: "core-notification error",
            onClose: () => {
              setPageLoading2(false)
            }
          });
        });
      }
    })
  }

  const {
    match: {
      params: {type}
    }
  } = props;

  return (
    <Formik
      initialValues={initialValues}
      validationSchema={validationSchema}
      onSubmit={onHandleSubmit}
      enableReinitialize
    >
      {(form) => {
        const {
          values,
          errors,
          handleSubmit,
          setFieldValue,
          setErrors,
        } = form;

        return (
          <Layout title="Admin" className="login h-screen flex-col">
            <div className="breadcrumb-user w-full flex items-center justify-between">
              <div className="flex items-center">
                <div className="cursor-pointer h-full flex items-center justify-center"
                     onClick={() => history?.goBack()}>
                  <img src={BackIcon} width={24}/>
                </div>
                <div className="ml-4">
                  <div className="pa-16 font-bold uppercase">{info?.email || info?.id || ""}</div>
                  <div className="uppercase"
                       style={{fontSize: 9, color: "#9e9e9e", letterSpacing: "1.35px"}}>users/{info?.email || info?.id || ""}</div>
                </div>
              </div>
              <div className="flex justify-end items-center">
                <UIButton
                  onClick={remove}
                  className="border capitalize filled-error mr-3">
                  remove
                </UIButton>
                <div className="text-white div mr-4 border-0 border-r border-solid border-gray-400">
                  l
                </div>
                <UIButton
                  onClick={() => history?.goBack()}
                  className="border capitalize filled-error mr-4">
                  cancel
                </UIButton>
                <UIButton
                  disabled={true}
                  // htmlType="submit"
                  className="third capitalize filled-error hidden">
                  {isLoading && <LoadingIcon/>}
                  save
                </UIButton>
              </div>
            </div>
            <div className="flex justify-center flex">
              <Container>
                <Row>
                  <Col className="col-sm-3"/>
                  <Col className="col-sm-6">
                    <div className="login-content">
                      <Form onFinish={handleSubmit}>
                        <div className="title-login">
                          Profile
                        </div>
                        <div className="joined-at">
                          Joined at {moment(info?.createAt).format("DD/MM/YYYY")}
                        </div>
                        <Form.Item
                          className="core-form-item w-full block mb-3"
                          label="Email"
                          hasFeedback={!!errors[fields.email]}
                          validateStatus={errors[fields.email] && "error"}
                          help={errors[fields.email]}
                        >
                          <Input
                            disabled={type !== "users"}
                            name={fields.email}
                            placeholder="email@example.com"
                            value={values[fields.email]}
                            onChange={({target: {value}}) => {
                              // setFieldValue(fields.email, value, false);
                              // removeError({
                              //   errors,
                              //   name: fields.email,
                              //   setErrors,
                              // });
                            }}
                          />
                        </Form.Item>
                        <div className="flex">
                          <div className="box mr-1">
                            <div className="box-title uppercase">
                              download
                            </div>
                            <div className="desc uppercase">
                              {formatMB(info?.traffic?.download || 0)} mb
                            </div>
                          </div>
                          <div className="box ml-1">
                            <div className="box-title uppercase">
                              upload
                            </div>
                            <div className="desc uppercase">
                              {formatMB(info?.traffic?.upload || 0)} mb
                            </div>
                          </div>
                        </div>
                      </Form>
                    </div>
                    {
                      error && (
                        <div className="core-alert flex items-center">
                          <div>
                            <i className="far fa-exclamation-triangle"/>
                          </div>
                          <div>{error}</div>
                        </div>
                      )
                    }
                  </Col>
                  <Col className="col-sm-3"/>
                </Row>
              </Container>
            </div>
            {isPageLoading && <LoadingPage/>}
            {isPageLoading2 && <LoadingPage2/>}
            <style jsx>{styles}</style>
          </Layout>
        )
      }}
    </Formik>
  )
}

export default withRouter(UserDetail)