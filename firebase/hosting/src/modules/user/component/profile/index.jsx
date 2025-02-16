import React from "react";

const md5 = require('md5');
import {Formik} from "formik";
import {Form, Input, notification} from "antd";
import * as Yup from "yup";
import css from 'styled-jsx/css'

import {LoadingIcon} from "@app/components/core/loading-icon";
import Container from "@app/components/core/container";
import Row from "@app/components/core/row";
import Col from "@app/components/core/col";
import UIButton from "@app/components/core/button";
import {withRouter} from "react-router";
import {auth, firestore} from "@app/services/firebase";
import {LocalStore} from "@app/utils/local-storage";
import {EMAIL, firebaseConfig} from "@app/configs";
import {LoadingPage} from "@app/components/core/loading";
import Layout from "@app/components/layout";
import {loadUser, setUserInfo} from "@app/redux/actions";
import {connect} from "react-redux";

const styles = css.global`
  .login {
    .login-content {
      width: 416px;
      height: fit-content;
      border-radius: 4px;
      border: solid 1px #e0e0e0;
      background-color: #ffffff;
      padding: 16px;
      margin: auto;
      margin-bottom: 16px;
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
  }
`

const Profile = ({location, history, ...props}) => {
  const [info, setInfo] = React.useState({})
  const [isLoading, setLoading] = React.useState(false)
  const [isPageLoading, setPageLoading] = React.useState(true)
  const fields = {
    email: "email",
    password: "password",
    first_name: "first_name",
    last_name: "last_name",
    newPasswordConfirm: "passwordConfirm",
    newPassword: "newPassword",
  };

  const [initialValues, setInitialValues] = React.useState({
    [fields.email]: "",
    [fields.password]: "",
    [fields.first_name]: "",
    [fields.last_name]: "",
    [fields.newPassword]: "",
    [fields.newPasswordConfirm]: "",
  })

  const validationSchema = Yup.object({
    [fields.password]: Yup.string()
      .required("Please enter a password")
      .min(8, "Must contain 8 characters"),
    [fields.newPassword]: Yup.string()
      .required("Please enter a new password")
      .min(8, "Must contain 8 characters"),
    [fields.newPasswordConfirm]: Yup.string().required("Please enter a new password confirm").oneOf(
      [Yup.ref('newPassword'), null],
      'Passwords must match',
    ),
  });

  const validationProfileSchema = Yup.object({
    [fields.email]: Yup.string()
      .email("Invalid email")
      .required("Please enter your email"),
    [fields.first_name]: Yup.string()
      .required("Please enter your first name"),
    [fields.last_name]: Yup.string()
      .required("Please enter your last name"),
  });

  const onHandleSubmit = async (formValues) => {
  };

  const removeError = ({errors, name, setErrors}) => {
    const newErrors = {...errors};
    delete newErrors?.[name];
    setErrors({...newErrors});
  };

  React.useEffect(() => {
    const currentUser = auth?.currentUser

    firestore.collection("admin").get()
      .then((result) => {
        setPageLoading(false)
        if (result.docs.length === 0) {
          auth.signOut().then(() => {
            this.setState({loading: false}, () => {
              LocalStore.local.set(firebaseConfig.projectId, "")
              history.push('/login')
            })
          }).catch(function (error) {});
        } else if(currentUser?.email && currentUser?.email?.toLowerCase() !== EMAIL) {
          loadProfile()
        } else {
          history.push('/server')
        }
      })
  }, [])

  const loadProfile = () => {
    const {
      setUserInfo
    } = props;

    firestore.collection("admin").where("email", "==", auth.currentUser?.email).get()
      .then((result) => {
        let tmp = result?.docs?.[0]?.data?.() || {}
        tmp = {
          ...tmp,
          id: result?.docs?.[0]?.id || ""
        }

        setInfo(tmp)
        setUserInfo(tmp)
        setInitialValues({
          ...initialValues,
          first_name: tmp?.first_name,
          last_name: tmp?.last_name,
          email: tmp?.email
        })
      })
  }

  const saveProfile = (formData) => {
    setLoading({
      ...isLoading,
      profile: true
    })
    if (info?.id) {
      firestore.collection("admin").doc(info?.id).update({
        first_name: formData?.first_name,
        last_name: formData?.last_name,
      })
        .then(() => {
          setLoading({
            ...isLoading,
            profile: false
          })
          notification.info({
            description: `Profile was updated successfully`,
            placement: "bottomRight",
            duration: 2,
            icon: "",
            className: "core-notification info",
            onClose: () => {
              loadProfile()
            }
          });
        })
        .catch(() => {
          setLoading({
            ...isLoading,
            profile: false
          })
          notification.info({
            description: `Profile was updated failure`,
            placement: "bottomRight",
            duration: 2,
            icon: "",
            className: "core-notification error",
          });
        })
    }
  }

  const changePass = (formData) => {
    setLoading({
      ...isLoading,
      password: true
    })
    if (info?.password !== md5(formData?.password)) {
      notification.info({
        description: `Old password is wrong!`,
        placement: "bottomRight",
        duration: 2,
        icon: "",
        className: "core-notification error",
        onClose: () => {
          setLoading({
            ...isLoading,
            password: false
          })
        }
      });
    } else {
      const user = auth.currentUser;

      user.updatePassword(formData?.newPassword).then(() => {
        firestore.collection("admin").doc(info?.id).update({
          password: md5(formData?.newPassword)
        })
          .then(() => {
            setLoading({
              ...isLoading,
              password: false
            })
            notification.info({
              description: `Password was updated successfully`,
              placement: "bottomRight",
              duration: 2,
              icon: "",
              className: "core-notification info",
              onClose: () => {
                setInitialValues({
                  ...initialValues,
                  newPassword: "",
                  newPasswordConfirm: "",
                  password: "",
                })
                loadProfile()
              }
            });
          })
          .catch((error) => {
            setLoading({
              ...isLoading,
              password: false
            })
            notification.info({
              description: error.message,
              placement: "bottomRight",
              duration: 2,
              icon: "",
              className: "core-notification error",
            });
          })
      }).catch((error) => {
        notification.info({
          description: error.message,
          placement: "bottomRight",
          duration: 2,
          icon: "",
          className: "core-notification error",
          onClose: () => {
            setLoading({
              ...isLoading,
              password: false
            })
          }
        });
      });
    }
  }

  return (
    <Layout title="Admin" className="login flex justify-center h-screen">
      <Container>
        <Row>
          <Col className="col-sm-3"/>
          <Col className="col-sm-6">
            <Formik
              initialValues={initialValues}
              validationSchema={validationProfileSchema}
              onSubmit={(e) => {
                saveProfile(e)
              }}
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
                  <Form onFinish={handleSubmit}>
                    <div className="login-content">
                      <div className="title-login">
                        Profile
                      </div>
                      <div className="flex">
                        <Form.Item
                          className="core-form-item w-full block mb-3 mr-2 flex-1"
                          label="First name"
                          hasFeedback={!!errors[fields.first_name]}
                          validateStatus={errors[fields.first_name] && "error"}
                          help={errors[fields.first_name]}
                        >
                          <Input
                            name={fields.first_name}
                            placeholder="First name"
                            value={values[fields.first_name]}
                            onChange={({target: {value}}) => {
                              setFieldValue(fields.first_name, value, false);
                              removeError({
                                errors,
                                name: fields.first_name,
                                setErrors,
                              });
                            }}
                          />
                        </Form.Item>
                        <Form.Item
                          className="core-form-item w-full block mb-3 ml-2 flex-1"
                          label="Last name"
                          hasFeedback={!!errors[fields.last_name]}
                          validateStatus={errors[fields.last_name] && "error"}
                          help={errors[fields.last_name]}
                        >
                          <Input
                            name={fields.last_name}
                            placeholder="Last name"
                            value={values[fields.last_name]}
                            onChange={({target: {value}}) => {
                              setFieldValue(fields.last_name, value, false);
                              removeError({
                                errors,
                                name: fields.last_name,
                                setErrors,
                              });
                            }}
                          />
                        </Form.Item>
                      </div>
                      <Form.Item
                        className="core-form-item w-full block mb-3"
                        label="Email"
                        hasFeedback={!!errors[fields.email]}
                        validateStatus={errors[fields.email] && "error"}
                        help={errors[fields.email]}
                      >
                        <Input
                          disabled
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
                      <div className="flex mt-10 justify-end">
                        <UIButton
                          disabled={isLoading?.profile}
                          htmlType="submit"
                          className="third capitalize filled-error w-1/3">
                          {isLoading?.profile && <LoadingIcon/>}
                          save change
                        </UIButton>
                      </div>
                    </div>
                  </Form>
                );
              }}
            </Formik>
            <Formik
              initialValues={initialValues}
              validationSchema={validationSchema}
              enableReinitialize
              onSubmit={changePass}
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
                  <Form onFinish={handleSubmit}>
                    <div className="login-content">
                      <div className="title-login">
                        Change password
                      </div>
                      <Form.Item
                        label="old password"
                        className="core-form-item w-full block mb-3"
                        validateStatus={
                          errors[fields.password] && "error"
                        }
                        help={errors[fields.password]}
                      >
                        <Input
                          type="password"
                          name={fields.password}
                          placeholder="*****"
                          value={values[fields.password]}
                          onChange={({target: {value}}) => {
                            setFieldValue(fields.password, value, false);
                            removeError({
                              errors,
                              name: fields.password,
                              setErrors,
                            });
                          }}
                        />
                      </Form.Item>

                      <Form.Item
                        label="new password"
                        className="core-form-item w-full block mb-3"
                        validateStatus={
                          errors[fields.newPassword] && "error"
                        }
                        help={errors[fields.newPassword]}
                      >
                        <Input
                          type="password"
                          name={fields.newPassword}
                          placeholder="*****"
                          value={values[fields.newPassword]}
                          onChange={({target: {value}}) => {
                            setFieldValue(fields.newPassword, value, false);
                            removeError({
                              errors,
                              name: fields.newPassword,
                              setErrors,
                            });
                          }}
                        />
                      </Form.Item>
                      <Form.Item
                        label="confirm new password"
                        className="core-form-item w-full block"
                        validateStatus={
                          errors[fields.newPasswordConfirm] && "error"
                        }
                        help={errors[fields.newPasswordConfirm]}
                      >
                        <Input
                          type="password"
                          name={fields.newPasswordConfirm}
                          placeholder="*****"
                          value={values[fields.newPasswordConfirm]}
                          onChange={({target: {value}}) => {
                            setFieldValue(fields.newPasswordConfirm, value, false);
                            removeError({
                              errors,
                              name: fields.newPasswordConfirm,
                              setErrors,
                            });
                          }}
                        />
                      </Form.Item>
                      <div className="flex mt-10 justify-end">
                        <UIButton
                          disabled={isLoading?.password}
                          htmlType="submit"
                          className="third capitalize filled-error w-1/3">
                          {isLoading?.password && <LoadingIcon/>}
                          save change
                        </UIButton>
                      </div>
                    </div>
                  </Form>
                );
              }}
            </Formik>
          </Col>
          <Col className="col-sm-3"/>
        </Row>
      </Container>
      {isPageLoading && <LoadingPage/>}
      <style jsx>{styles}</style>
    </Layout>
  )
}

const mapDispatchToProps = ({
  setUserInfo
})

export default connect(null, mapDispatchToProps)(withRouter(Profile));